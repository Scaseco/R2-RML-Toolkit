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

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.TermSpec;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
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
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.sse.Tags;
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

    protected MappingCxt mappingCxt;

    // The triples / quads constructed from the triples map
    protected Template template;

    // The mapping of variables to the term maps from which they were derived
    protected Map<TermSpec, Var> termMapToVar;

    // The mapping for term maps' variables to the corresponding sparql expression
    // E.g. a rr:template "foo{bar}" becomes IRI(CONCAT("foo", STR(?var)))
    // protected VarExprList varToExpr;
    protected GenericDag<Expr, Var> exprDag;

    protected List<JoinDeclaration> joins;

    public TriplesMapToSparqlMapping(TriplesMap triplesMap, MappingCxt mappingCxt, Template template, Map<TermSpec, Var> termMapToVar,
            GenericDag<Expr, Var> exprDag, List<JoinDeclaration> joins) {
        super();
        this.triplesMap = triplesMap;
        this.mappingCxt = mappingCxt;
        this.template = template;
        this.termMapToVar = termMapToVar;
        this.exprDag = exprDag;
        this.joins = joins;
    }

    // XXX Tried to rename to getExpandedVarExprList
    // However this method is referenced by sparqlify - so for now we need to keep the name for legacy reasons
    public VarExprList getVarToExpr() {
        return getVarToExpr(true, true);
    }

    /**
     * Creates a var expr list where every variable root node maps to the full expansion of its definition -
     * the expansion includes only variables that are undefined.
     *
     * If there were common sub-expressions then they will be evaluated repeatedly.
     *
     * @param includeIdentities
     *               Whether to include entries such as (?x, ?x).
     *               If false then it is returned as (?x, null)
     * @param legacy Changes the semantics as follows
     *               (1) strdt to cast: strdt(X, Y) becomes Y(X)
     *               (2) no str: str(X) becomes X
     * @return
     */
    public VarExprList getVarToExpr(boolean includeIdentities, boolean legacy) {
        VarExprList result = new VarExprList();
        for (Expr root : exprDag.getRoots()) {
            if (root.isVariable()) {
                Var var = root.asVar();
                Expr expansion = GenericDag.expand(exprDag, root);

                if (legacy) {
                    expansion = ExprUtils.replace(expansion, e -> {
                        Expr r = e;
                        E_Str str = ObjectUtils.castAsOrNull(E_Str.class, e);
                        E_StrDatatype strdt = null; //ObjectUtils.castAsOrNull(E_StrDatatype.class, e);
                        if (str != null) {
                            r = str.getArg();
                        } else if (strdt != null && strdt.getArg2().isConstant()) {
                            r = new E_Function(strdt.getArg2().getConstant().asNode().getURI(), new ExprList(strdt.getArg1()));
                        }
//                        E_StrConcat concat = ObjectUtils.castAsOrNull(E_StrConcat.class, e);
//                        else if (concat != null) {
//                            List<Expr> newArgs = concat.getArgs().stream()
//                                    .map(arg -> arg instanceof E_Str ? ((E_Str)arg).getArg() : arg)
//                                    .collect(Collectors.toList());
//                            r = new E_StrConcat(new ExprList(newArgs));
//                        }
                        return r;
                    });
                }

                if (expansion.isVariable() && var.equals(expansion.asVar()) && !includeIdentities) {
                    result.add(var);
                } else {
                    result.add(var, expansion);
                }
            }
        }
        return result;
    }

    public TriplesMap getTriplesMap() {
        return triplesMap;
    }

    public MappingCxt getMappingCxt() {
        return mappingCxt;
    }

    public Template getTemplate() {
        return template;
    }

    public Map<TermSpec, Var> getTermMapToVar() {
        return termMapToVar;
    }

    public GenericDag<Expr, Var> getExprDag() {
        return exprDag;
    }

    public List<JoinDeclaration> getJoins() {
        return joins;
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
        // TODO Update the code to evaluate on the dag
        VarExprList vel = getVarToExpr(true, false);
        return evalVars(vel, binding, env, strictIriValidation);
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

            // TODO Probably this code should be updated to simply rely on the custom function F_BnodeAsGiven
            // if (expr instanceof E_BNode) { // No longer works
            if (expr.isFunction() &&  Tags.tagBNode.equals(expr.getFunction().getFunctionSymbol().getSymbol())) {
                // Special handling of bnodes
                // For each variable that maps to a bnode definition keep a mapping from the argument value
                // to the generated bnode
                ExprFunction ebnode = (ExprFunction)expr;
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
        return getAsQuery(false);
    }

    /**
     * Express this mapping as a tarql-like query. Does not include joins.
     *
     * @param safeVars If true then the result is tarql compatible.
     * Characters that are illegal in SPARQL variable names are replaced with underscore.
     *
     * @return
     */
    public Query getAsQuery(boolean safeVars) {
        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(template);

        ElementGroup elt = new ElementGroup();

        VarExprList varToExpr = getVarToExpr(false, false);

        // for (Entry<Var, Expr> e : varToExpr.entrySet()) {
        varToExpr.forEachVarExpr((v, e) ->  {
            if (e != null) {
                Expr ee = !safeVars
                        ? e
                        : ExprTransformer.transform(new NodeTransformExpr(n -> n.isVariable() ? VarUtils.safeVar(n.getName()) : n), e);
                elt.addElement(new ElementBind(v, ee));
            }
        });
        result.setQueryPattern(elt);

        // Copying prefixes is not that useful because it can lead to huge prefix declarations
        // which makes queries hard to read
        // It seems better to let the application handle this
        // result.setPrefixMapping(triplesMap.getModel());

        LogicalTable logicalTable = triplesMap.getLogicalTable();
        if (logicalTable != null && triplesMap.getLogicalTable().qualifiesAsBaseTableOrView()) {
            String tableName = logicalTable.asBaseTableOrView().getTableName();
            if (tableName != null) {
                result.getGraphURIs().add(tableName);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return getAsQuery().toString();
    }
}
