package org.aksw.r2rml.jena.arq.impl;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.JoinCondition;
import org.aksw.r2rml.jena.domain.api.ObjectMapType;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.RefObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.XSD;

public class TriplesMapProcessorR2rml {
    protected TriplesMap triplesMap;
    protected String baseIri;

    /** VarAlloc for generating variables that represent the set of records of a logical source */
    protected VarAlloc sourceVarGen = new VarAlloc("s");

    /** The context for *this* triple maps. Parent contexts are created when there are joins. */
    protected MappingCxt childCxt;

    public TriplesMapProcessorR2rml(TriplesMap triplesMap, String baseIri) {
        // this(tm, new VarAlloc("v"), HashBiMap.create(), new HashMap<>(), new QuadAcc());
        this.triplesMap = triplesMap;
        this.baseIri = baseIri;
    }

    /**
     *
     * @param processPoms Whether to process predicate object maps. If false then only the subject map will be processed which is useful to process the parent side of a join.
     * @return
     */
    public TriplesMapToSparqlMapping call() {
        // TODO shortcut expansion should already have happened
        R2rmlLib.expandShortcuts(triplesMap);

        Var triplesMapVar = sourceVarGen.allocVar();

        this.childCxt = new MappingCxt(null, triplesMap, triplesMapVar);

        SubjectMap sm = triplesMap.getSubjectMap();
        Node s = allocateVarTracked(childCxt, sm, RR.IRI);
        Objects.requireNonNull(sm, "SubjectMap was null on " + triplesMap);

        Set<GraphMap> sgms = sm.getGraphMaps();

        for(PredicateObjectMap pom : triplesMap.getPredicateObjectMaps()) {
            Set<GraphMap> pogms = pom.getGraphMaps();

            // egms = effective graph maps
            Set<GraphMap> egms = Sets.union(sgms, pogms);


            // A single graph without a name
            if(egms.isEmpty()) {
                egms = Collections.singleton(null);
            }

            Set<PredicateMap> pms = pom.getPredicateMaps();
            Set<ObjectMapType> oms = pom.getObjectMaps();

            for(GraphMap gm : egms) {
                Node g = gm == null ? RR.defaultGraph.asNode() : allocateVarTracked(childCxt, gm, RR.IRI);
                for(PredicateMap pm : pms) {
                    Node p = allocateVarTracked(childCxt, pm, RR.IRI);
                    for(ObjectMapType om : oms) {
                        if (!om.qualifiesAsRefObjectMap()) {
                            Node o = allocateVarTracked(childCxt, om.asTermMap(), RR.Literal);

                            // Template: creates triples using Quad.defaultGraphNodeGenerated
                            // RDFDataMgr.loadDataset: loads default graph tripls with Quad.defaultGraphIRI
                            // So the output does not match exactly...

                            Quad quad = createQuad(g, s, p, o);
                            childCxt.quadAcc.addQuad(quad);
                        } else {
                            RefObjectMap rom = om.asRefObjectMap();
                            processRefObjectMap(g, s, p, rom);
                        }
                    }
                }
            }
        }

//        VarExprList vel = new VarExprList();
//        childCxt.getExprDag().getVarToExpr().forEach(vel::add);
        childCxt.getExprDag().collapse();

        Template template = new Template(childCxt.quadAcc);
        TriplesMapToSparqlMapping result = new TriplesMapToSparqlMapping(
            triplesMap, childCxt, template, childCxt.termMapToVar, childCxt.getExprDag(), childCxt.joins);

        return result;
    }

    /**
     * Calls {@link #allocateVar(TermMap, Resource) and tracks the result in the #termMapToVar
     * If a variable maps to a constant expression then no entry is made to nodeToExpr.
     */
    protected Node allocateVarTracked(
            MappingCxt cxt,
            TermMap tm,
            Resource fallbackTermType) {
        Node result = allocateVar(cxt, tm, fallbackTermType);
        if (result.isVariable()) {
            cxt.termMapToVar.put(tm, (Var)result);
        }
        return result;
    }

    /** Allocates a variable for a given term map. */
    protected Node allocateVar(
            MappingCxt cxt,
            TermMap tm,
            Resource fallbackTermType) {
        Expr expr = termMapToExpr(cxt, tm, fallbackTermType);
        // expr = postProcessExpr(cxt, expr);
        Node result = allocateVarForExpr(cxt, expr);
        return result;
    }

    protected Node allocateVarForExpr(MappingCxt cxt, Expr expr) {
        Node result = cxt.getExprDag().getVar(expr);
        if(result == null) {
            // If the expr is a constant, just use its node as the result; no need to track this in the map
            if(expr.isConstant()) {
                result = expr.getConstant().asNode();
            } else {
                // Allocate a new variable
                Expr newRoot = cxt.getExprDag().addRoot(expr);
                Var v = ExprUtils.getExprOps().asVar(newRoot);
                // Preconditions.checkState(v != null, "Get null as the variable for a just added expression");
                result = v;
            }
        }

        return result;
    }

    /**
     * Convert a term map into a corresponding SPARQL expression
     *
     * @param tm
     * @return
     */
    protected Expr termMapToExpr(MappingCxt cxt, TermMap tm, Resource fallbackTermType) {
        Expr result;

        // In the following derive the effective term type based on the term map's attributes
        Node effectiveTermType = null;

        // If a datatype has been specified then get its node
        // and validate that its an IRI
        String template = tm.getTemplate();
        RDFNode constant = tm.getConstant();
        Node datatypeNode = R2rmlImporterLib.getIriNodeOrNull(tm.getDatatype());
        Node termTypeNode = R2rmlImporterLib.getIriNodeOrNull(tm.getTermType());

        String langValue = Optional.ofNullable(tm.getLanguage()).map(String::trim).orElse(null);

        // Infer the effective term type

        if (termTypeNode != null) {
            effectiveTermType = termTypeNode;
        }

        if (constant != null) {
            effectiveTermType = ObjectUtils.requireNullOrEqual(effectiveTermType, R2rmlImporterLib.classifyTermType(constant.asNode()).asNode());
        }

        if (langValue != null) {
            effectiveTermType = ObjectUtils.requireNullOrEqual(effectiveTermType, RR.Literal.asNode());
        }

        if (effectiveTermType == null && template != null) {
            effectiveTermType = RR.IRI.asNode();
        }

        if (effectiveTermType == null) {
            effectiveTermType = fallbackTermType.asNode();
        }

        // FIXME Add extension point for FunctionMap-like constructs where the term type
        // yet needs to be applied

        if((template = tm.getTemplate()) != null) {
            Expr rawArg = R2rmlTemplateLib.parse(template);

            // Resolve all variable names. This gives e.g. an RML processor the chance
            // to resolve all variable names (=references) against the reference formulation.
            Expr arg = resolveColumnReferences(cxt, rawArg);

            result = R2rmlImporterLib.applyTermType(arg, effectiveTermType, XSD.xstring.asNode());
        } else if((constant = tm.getConstant()) != null) {
            result = NodeValue.makeNode(constant.asNode());
        } else {
            Expr columnLikeExpr = resolveColumnLikeTermMap(cxt, tm, fallbackTermType);
            if(columnLikeExpr != null) {
                if (langValue != null) {
                    result = new E_StrLang(columnLikeExpr, NodeValue.makeString(langValue));
                } else {
                    result = R2rmlImporterLib.applyTermType(columnLikeExpr, effectiveTermType, datatypeNode);
                }
            } else {
                throw new RuntimeException("TermMap does neither define rr:template, rr:constant nor rr:column " + tm);
            }
        }
        // result = postProcessExpr(cxt, result);
        return result;
    }

    /** Transform references from rr:template or rr:column */
    protected Expr resolveColumnReferences(MappingCxt cxt, Expr columnExpr) {

        // Resolve all variable names. This gives e.g. an RML processor the chance
        // to resolve all variable names (=references) against the reference formulation.
        Expr result = ExprTransformer.transform(new ExprTransformCopy() {
            @Override
            public Expr transform(ExprVar exprVar) {
                String varName = exprVar.getVarName();
                Expr r = referenceToExpr(cxt, varName);
                return r;
            }
        }, columnExpr);
        return result;
    }

    /** Override this method for RML termMap and references */
    protected Expr resolveColumnLikeTermMap(MappingCxt cxt, TermMap tm, Resource fallbackTermType) {
        Expr result = null;
        String colName = tm.getColumn();
        if(colName != null) {
            Expr rawArg = new ExprVar(colName);
            result = resolveColumnReferences(cxt, rawArg);
        }
        return result;
    }

    protected void processRefObjectMap(Node g, Node s, Node p, RefObjectMap rom) {
        TriplesMap parentTm = rom.getParentTriplesMap();
        SubjectMap parentSm = parentTm.getSubjectMap();

        Var parentVar = sourceVarGen.allocVar();
        MappingCxt parentCxt = new MappingCxt(childCxt, parentTm, parentVar);

        Node o = allocateVarTracked(parentCxt, parentSm, RR.IRI);

        Set<JoinCondition> joinConditions = rom.getJoinConditions();
        ExprList constraints = new ExprList();

        for (JoinCondition jc : joinConditions) {
            String parentStr = jc.getParent();
            String childStr = jc.getChild();
            Expr parentExpr = referenceToExpr(parentCxt, parentStr);
            Expr childExpr = referenceToExpr(childCxt, childStr);
            E_Equals constraint = new E_Equals(parentExpr, childExpr);
            constraints.add(constraint);
        }

        // Eliminate self join: (Ideally this would be handled on the SPARQL algebra level)
        // Applicable if:
        // - parent and child use the same logical source
        // - there is a join on the same columns (e.g. id = id)
        // - either childVars or parentVars references no columns besides the join columns
        Object parentId = getSourceIdentity(parentTm);
        Object childId = getSourceIdentity(triplesMap);

        boolean isEliminated = false;

        if (parentId != null && parentId.equals(childId)) {
            // Check whether all constraints are identical when substituting the source variable
            NodeTransform parentToChildSrcVar = n -> parentCxt.getTriplesMapVar().equals(n) ? childCxt.getTriplesMapVar() : n;
            ExprList toCheck = constraints.applyNodeTransform(parentToChildSrcVar);
            boolean isIdentity = toCheck.getList().stream().map(e -> (E_Equals)e).allMatch(e -> Objects.equals(e.getArg1(), e.getArg2()));

            if (isIdentity) {
                Set<Expr> joinExprs = toCheck.getList().stream().map(e -> (E_Equals)e).map(e -> e.getArg1()).collect(Collectors.toSet());

                // Check if by substitution of all join expressions no further expressions making use of the child/parent source variable remain
                // ExprTransform et = new ExprTransformBase()

                // This is a bit of ugly back and forth: We decompose expressions into the dag
                // but here we need to check whether their expanded forms are equal
                Expr childSubjectExpr = GenericDag.expand(childCxt.getExprDag(), childCxt.getSubjectDefinition().getExpr());
                Expr parentSubjectExpr = GenericDag.expand(parentCxt.getExprDag(), parentCxt.getSubjectDefinition().getExpr());
                Expr newParentSubjectExpr = parentSubjectExpr.applyNodeTransform(parentToChildSrcVar);

                Expr placeholder = NodeValue.makeString("placeholder");
                Function<Expr, Expr> exprTransform =  e -> joinExprs.contains(e) ? placeholder : e;

                Expr replacedChildExpr = ExprUtils.replace(childSubjectExpr, exprTransform);
                Expr replacedParentExpr = ExprUtils.replace(newParentSubjectExpr, exprTransform);

                Set<Var> remainingChildSubjectVars = replacedChildExpr.getVarsMentioned();
                Set<Var> remainingParentSubjectVars = replacedParentExpr.getVarsMentioned();

//                System.out.println(replacedChildExpr);
//                System.out.println(replacedParentExpr);

                if (remainingChildSubjectVars.isEmpty()) {
                    isEliminated = true;
                    Var newParentVar = (Var)allocateVarForExpr(parentCxt, newParentSubjectExpr);
                    if (newParentVar.isVariable()) {
                        childCxt.termMapToVar.put(rom, newParentVar);
                        childCxt.getExprDag().getVarToExpr().put(newParentVar, replacedParentExpr);
                    }
                    childCxt.getQuadAcc().addQuad(createQuad(g, s, p, newParentVar));
                } else if (remainingParentSubjectVars.isEmpty()) {
                    isEliminated = true;
                    // Expr newParentExpr = newParentSubjectExpr.applyNodeTransform(parentToChildSrcVar);
                    Var newParentVar = (Var)allocateVarForExpr(parentCxt, newParentSubjectExpr);
                    if (newParentVar.isVariable()) {
                        childCxt.termMapToVar.put(rom, newParentVar);
                        childCxt.getExprDag().getVarToExpr().put(newParentVar, replacedParentExpr);
                    }
                    childCxt.getQuadAcc().addQuad(createQuad(g, newParentVar, p, s));
                }
            }
        }

        if (!isEliminated) {
            Quad quad = createQuad(g, s, p, o);
            parentCxt.getExprDag().collapse();
            JoinDeclaration join = new JoinDeclaration(parentCxt, null, rom, quad, constraints);
            childCxt.joins.add(join);
        }
    }

    /**
     * Return an object that represents the identity of a triplesmaps' source.
     * Used for self-join elimination.
     *
     * By default the triples map is returned as representing it's source.
     * This way however misses optimization oppor, two triples maps with the same logical source will
     */
    protected Object getSourceIdentity(TriplesMap tm) {
        return tm;
    }

    protected Quad createQuad(Node g, Node s, Node p, Node o) {
        Node finalG = RR.defaultGraph.asNode().equals(g)
                ? Quad.defaultGraphNodeGenerated
                : g;

        return Quad.create(finalG, s, p, o);
    }

    /**
     * Extension point for resolving RML references.
     * The R2RML processor ignores the source.
     *
     * @param source A variable that is bound to the source records and thus represents the source.
     * @param colName A column name or more generally a reference expression string.
     * @return
     */
    protected Expr referenceToExpr(MappingCxt cxt, String colName) {
        ExprVar column = new ExprVar(colName);
        return column;
    }
}
