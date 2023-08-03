package org.aksw.rml.jena.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jenax.model.shacl.util.ShPrefixUtils;
import org.aksw.jenax.stmt.parser.element.SparqlElementParser;
import org.aksw.jenax.stmt.parser.element.SparqlElementParserImpl;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.model.QlTerms;
import org.aksw.rmlx.model.RmlAlias;
import org.aksw.rmlx.model.RmlDefinitionBlock;
import org.aksw.rmlx.model.RmlQualifiedBind;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.util.ResourceUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class RmlDefinitionBlockUtils {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        RmlDefinitionBlock block = model.createResource().as(RmlDefinitionBlock.class);
        Set<RmlQualifiedBind> binds = block.getQualifiedBinds();

        binds.add(model.createResource().as(RmlQualifiedBind.class)
                .setLabel("a")
                .setDefinition("?d"));

        binds.add(model.createResource().as(RmlQualifiedBind.class)
                .setLabel("d")
                .setDefinition("?b"));

        binds.add(model.createResource().as(RmlQualifiedBind.class)
                .setLabel("b")
                .setDefinition("?c"));

        ReferenceFormulation rf = ReferenceFormulationRegistry.get().get(QlTerms.CSV);
        VarExprList vel = processExprs(block, str -> rf.reference(Var.alloc("s0"), str));
        System.out.println(vel);

    }


    /** Method to copy LogicalSources without the definition block*/
    public static Resource closureWithoutDefinitions(Resource r) {
        // Create a relevant copy of the logical source that has aliases/binds cleared
        Model closure1 = ResourceUtils.reachableClosure(r);
        RmlDefinitionBlock block = r.inModel(closure1).as(RmlDefinitionBlock.class);

        block.getAliases().clear();
        block.getBinds().clear();
        block.getPrefixes().clear();
        block.getQualifiedBinds().clear();

        // After clearing the attributes create another closure
        Model closure2 = ResourceUtils.reachableClosure(block);
        Resource result = r.inModel(closure2);
        return result;
    }

    public static VarExprList processExprs(RmlDefinitionBlock block, Function<String, Expr> refResolver) {

        // Set<PrefixSet> prefixSets = block.getPrefixes();
        PrefixMap prefixMap = ShPrefixUtils.collect(block);
        PrefixMapping prefixMapping = new PrefixMappingAdapter(prefixMap);

        // Set<String> aliases = new LinkedHashSet<>();
        // BiMap<String, Var> nameToVar = HashBiMap.create();

        VarExprList result = new VarExprList();
        Set<Var> definedVars = new HashSet<>();

        // Process aliases - we don't have to take order into account
        Set<RmlAlias> aliases = block.getAliases();
        for (RmlAlias alias : aliases) {
            String name = alias.getLabel();
            Var var = Var.alloc(name);
            // nameToVar.put(name, var);

            String defStr = alias.getDefinition();
            Expr expr = refResolver.apply(defStr);

            result.add(var, expr);
            definedVars.add(var);
        }

        // Process sparql definitions
        Map<Var, Expr> rawDefs = new HashMap<>();
        Set<RmlQualifiedBind> qualifiedBinds = block.getQualifiedBinds();
        for (RmlQualifiedBind bind : qualifiedBinds) {
            // Try to parse each expression
            String name = bind.getLabel();
            Var var = Var.alloc(name);
            String defStr = bind.getDefinition();

            Expr expr;
            try {
                expr = ExprUtils.parse(defStr, prefixMapping);
            } catch (QueryParseException e) {
                throw new QueryParseException("Failed to parse definition for [" + name + "]: " + defStr, e, e.getLine(), e.getColumn());
            }
            rawDefs.put(var, expr);
            definedVars.add(var);
        }

        Set<String> binds = block.getBinds();
        SparqlElementParser eltParser = SparqlElementParserImpl.create(Syntax.syntaxARQ, new Prologue(prefixMapping));
        for (String bind : binds) {
            String str = "BIND(" + bind + ")";

            // Try to parse each expression
            ElementBind elt;
            try {
                elt = (ElementBind)eltParser.apply(str);
            } catch (QueryParseException e) {
                throw new QueryParseException("Failed to parse definition: " + bind, e, e.getLine(), e.getColumn());
            }
            Var var = elt.getVar();
            Expr expr = elt.getExpr();

            rawDefs.put(var, expr);
            definedVars.add(var);
        }

        DefaultDirectedGraph<Var, DefaultEdge> dag = DefaultDirectedGraph.<Var, DefaultEdge>createBuilder(DefaultEdge.class).build();
        for (Entry<Var, Expr> e : rawDefs.entrySet()) {
            Var v = e.getKey();
            dag.addVertex(v);
            Expr expr = e.getValue();
            Set<Var> dependencies = expr.getVarsMentioned();
            for (Var dependency : dependencies) {
                if (!dependency.equals(v)) {
                    dag.addVertex(dependency);
                    dag.addEdge(dependency, v);
                }
            }
        }

        TopologicalOrderIterator<Var, DefaultEdge> iterator = new TopologicalOrderIterator<>(dag);
        while (iterator.hasNext()) {
            Var var = iterator.next();
            Expr rawExpr = rawDefs.get(var);

            // If the expression mentions variable being defined then pass it through the reference formulation
            String varName = var.getName();
            ExprVar ev = new ExprVar(var);
            Expr expr = org.aksw.jenax.arq.util.expr.ExprUtils.replace(rawExpr, e -> {
                Expr r = ev.equals(e)
                        ? refResolver.apply(varName)
                        : e;
                return r;
            });

            // Process the vertex
            // System.out.println(vertex);
            if (expr != null) {
                result.add(var, expr);
                definedVars.add(var);
            } else if (!definedVars.contains(var)) {
                // If a variable was not defined then try to treat it as a reference
                String refStr = var.getName();
                Expr refExpr = refResolver.apply(refStr);
                result.add(var, refExpr);
                definedVars.add(var);
            }
        }

        return result;
    }
}

//List<Var> roots = JGraphUtils.findRoots(dag);
//class JGraphUtils {
//    public static <V, E> List<V> findRoots(Graph<V, E> graph) {
//        return graph.vertexSet().stream()
//                .filter(v -> graph.inDegreeOf(v) == 0)
//                .collect(Collectors.toList());
//    }
//}
//
