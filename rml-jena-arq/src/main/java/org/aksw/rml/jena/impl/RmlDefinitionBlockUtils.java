package org.aksw.rml.jena.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jenax.model.prefix.domain.api.PrefixSet;
import org.aksw.jenax.model.prefix.domain.api.PrefixSetUtils;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.model.QlTerms;
import org.aksw.rmlx.model.RmlAlias;
import org.aksw.rmlx.model.RmlBind;
import org.aksw.rmlx.model.RmlDefinitionBlock;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;
import org.apache.jena.sparql.util.ExprUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class RmlDefinitionBlockUtils {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        RmlDefinitionBlock block = model.createResource().as(RmlDefinitionBlock.class);
        Set<RmlBind> binds = block.getBinds();

        binds.add(model.createResource().as(RmlBind.class)
                .setLabel("a")
                .setDefinition("?d"));

        binds.add(model.createResource().as(RmlBind.class)
                .setLabel("d")
                .setDefinition("?b"));

        binds.add(model.createResource().as(RmlBind.class)
                .setLabel("b")
                .setDefinition("?c"));

        ReferenceFormulation rf = ReferenceFormulationRegistry.get().get(QlTerms.CSV);
        VarExprList vel = processExprs(block, str -> rf.reference(Var.alloc("s0"), str));
        System.out.println(vel);

    }

    public static VarExprList processExprs(RmlDefinitionBlock block, Function<String, Expr> refResolver) {

        Set<PrefixSet> prefixSets = block.getPrefixSets();
        PrefixMap prefixMap = PrefixSetUtils.collect(prefixSets);
        PrefixMapping prefixMapping = new PrefixMappingAdapter(prefixMap);

        // Set<String> aliases = new LinkedHashSet<>();
        // BiMap<String, Var> nameToVar = HashBiMap.create();

        VarExprList result = new VarExprList();

        // Process aliases - we don't have to take order into account
        Set<RmlAlias> aliases = block.getAliases();
        for (RmlAlias alias : aliases) {
            String name = alias.getLabel();
            Var var = Var.alloc(name);
            // nameToVar.put(name, var);

            String defStr = alias.getDefinition();
            Expr expr = refResolver.apply(defStr);
            result.add(var, expr);
        }

        // Process sparql definitions
        Map<Var, Expr> rawDefs = new HashMap<>();
        Set<RmlBind> binds = block.getBinds();
        for (RmlBind bind : binds) {
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
        }

        DefaultDirectedGraph<Var, DefaultEdge> dag = DefaultDirectedGraph.<Var, DefaultEdge>createBuilder(DefaultEdge.class).build();
        for (Entry<Var, Expr> e : rawDefs.entrySet()) {
            Var v = e.getKey();
            dag.addVertex(v);
            Expr expr = e.getValue();
            Set<Var> dependencies = expr.getVarsMentioned();
            for (Var dependency : dependencies) {
                dag.addVertex(dependency);
                dag.addEdge(dependency, v);
            }
        }

        TopologicalOrderIterator<Var, DefaultEdge> iterator = new TopologicalOrderIterator<>(dag);
        while (iterator.hasNext()) {
            Var var = iterator.next();
            Expr expr = rawDefs.get(var);
            // Process the vertex
            // System.out.println(vertex);
            if (expr != null) {
                result.add(var, expr);
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
