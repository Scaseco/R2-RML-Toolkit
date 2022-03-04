package org.aksw.r2rml.jena.arq.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.ext.com.google.common.collect.HashBasedTable;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.ext.com.google.common.collect.Table;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIs;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.Symbol;

/**
 * A mapping of a single TriplesMap to the triples and SPARQL expressions
 * is corresponds to.
 *
 * @author Claus Stadler
 *
 */
public class TriplesMapToSparqlMapping {
    // The triples map from which this mapping was created
    protected TriplesMap triplesMap;

    // The triples / quads constructed from the triples map
    protected Template template;

    // The mapping of variables to the term maps from which they were derived
    protected Map<TermMap, Var> termMapToVar;

    // The mapping for term maps' variables to the corresponding sparql expression
    // E.g. a rr:template "foo{bar}" becomes IRI(CONCAT("foo", STR(?var)))
    protected VarExprList varToExpr;

    public TriplesMapToSparqlMapping(TriplesMap triplesMap, Template template, Map<TermMap, Var> termMapToVar,
            VarExprList varToExpr) {
        super();
        this.triplesMap = triplesMap;
        this.template = template;
        this.termMapToVar = termMapToVar;
        this.varToExpr = varToExpr;
    }

    public TriplesMap getTriplesMap() {
        return triplesMap;
    }

    public Template getTemplate() {
        return template;
    }

    public Map<TermMap, Var> getTermMapToVar() {
        return termMapToVar;
    }

    public VarExprList getVarToExpr() {
        return varToExpr;
    }

    public Stream<Quad> evalQuads(Binding effectiveBinding) {
        // Binding eb = evalVars(binding);
        Iterator<Quad> it = TemplateLib.calcQuads(
                template.getQuads(),
                Collections.singleton(effectiveBinding).iterator());
        return Streams.stream(it);
    }

    public Stream<Triple> evalTriples(Binding effectiveBinding) {
        // Binding eb = evalVars(binding);
        Iterator<Triple> it = TemplateLib.calcTriples(
                template.getTriples(),
                Collections.singleton(effectiveBinding).iterator());
        return Streams.stream(it);
    }

    public Binding evalVars(Binding binding, FunctionEnv env, boolean strictIriValidation) {
        return evalVars(varToExpr, binding, env, strictIriValidation);
    }



    public static final Symbol BnodeTracker = Symbol.create("bnodeTracker");

    static class BnodeTrackerGlobal {
        protected Map<Object, Node> argToBnode;

        public BnodeTrackerGlobal() {
            super();
            argToBnode = new HashMap<>();
        }

        public Map<Object, Node> getMap() {
            return argToBnode;
        }

    }

    static class BnodeTrackerPerVar {
        protected Table<Var, List<Node>, Node> varToArgToNode;

        public BnodeTrackerPerVar() {
            super();
            varToArgToNode = HashBasedTable.create();
        }

        public Table<Var, List<Node>, Node> getTable() {
            return varToArgToNode;
        }
    }

    /**
     *
     * @param varToExpr
     * @param binding
     * @param env
     * @param strictIriValidation If true then every generated IRI is checked for validity and exceptions are raised accordingly.
     *
     * @return
     */
    public static Binding evalVars(
            VarExprList varToExpr,
            Binding binding,
            FunctionEnv env,
            boolean strictIriValidation) {
        BindingBuilder builder = BindingFactory.builder();
        for (Entry<Var, Expr> e : varToExpr.getExprs().entrySet()) {

            Var v = e.getKey();
            Expr expr = e.getValue();

            Node node;

            if (expr instanceof E_BNode) {
                // Special handling of bnodes
                // For each variable that maps to a bnode definition keep a mapping from the argument value
                // to the generated bnode
                E_BNode ebnode = (E_BNode)expr;
                // Expr bexpr = ebnode.getExpr();
                List<Expr> args = ebnode.getArgs();
                Object argObj;
                switch (args.size()) {
                case 0:
                    argObj = Collections.emptyList();
                    break;
                case 1:
                    // Node.ANY as a null replacement
                    argObj = Optional.ofNullable(safeEval(args.get(0), binding, env))
                        .map(NodeValue::asNode)
                        .orElse(Node.ANY);
                    break;
                default:
                    argObj = args.stream()
                        .map(arg -> safeEval(arg, binding, env))
                        .map(nv -> nv == null ? null : nv.asNode())
                        .collect(Collectors.toList())
                        .toArray(new Node[0]);
                    break;
                }

                BnodeTrackerGlobal tracker = env.getContext().get(BnodeTracker);
                if (tracker == null) {
                    tracker = new BnodeTrackerGlobal();
                    env.getContext().set(BnodeTracker, tracker);
                }

                Map<Object, Node> map = tracker.getMap();
                node = map.get(argObj);
                if (node == null) {
                    NodeValue nv = safeEval(ebnode, binding, env);
                    node = nv.asNode();
                    map.put(argObj, node);
                }

//				Map<List<Node>, Node> map = tracker.getTable().row(v);
//				node = map.get(argVals);
//				if (node == null) {
//					NodeValue nv = safeEval(ebnode, binding, env);
//					node = nv.asNode();
//					map.put(argVals, node);
//				}

            } else {
                NodeValue nv = safeEval(expr, binding, env);
                node = nv == null ? null : nv.asNode();
            }

            if (node != null) {
                if (node.isURI()) {
                    if (strictIriValidation) {
                        String iriStr = node.getURI();
                        // IRIResolver.validateIRI(iriStr);
                        IRIs.check(iriStr);
                    }
                }

                builder.add(v, node);
            }
        }

        return builder.build();
    }

    public static NodeValue safeEval(Expr expr, Binding binding, FunctionEnv env) {
        NodeValue nv = null;
        try {
            nv = expr.eval(binding, env);
        } catch (VariableNotBoundException ex) {
            // Just ignore
        } catch (ExprEvalException ex) {
            // Treat as evaluation to null
             // ex.printStackTrace();
            throw new RuntimeException("Eval exception", ex);
        }

        return nv;
    }

    public Query getAsQuery() {
        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(template);

        ElementGroup elt = new ElementGroup();
        // for (Entry<Var, Expr> e : varToExpr.entrySet()) {
        varToExpr.forEachVarExpr((v, e) ->  elt.addElement(new ElementBind(v, e)));
        result.setQueryPattern(elt);

        return result;
    }

    @Override
    public String toString() {
        return getAsQuery().toString();
    }
}
