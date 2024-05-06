package org.aksw.r2rml.jena.arq.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.r2rml.jena.vocab.RR;
import org.aksw.rmltk.model.backbone.common.IGraphMap;
import org.aksw.rmltk.model.backbone.common.IJoinCondition;
import org.aksw.rmltk.model.backbone.common.IObjectMapType;
import org.aksw.rmltk.model.backbone.common.IPredicateMap;
import org.aksw.rmltk.model.backbone.common.IPredicateObjectMap;
import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ISubjectMap;
import org.aksw.rmltk.model.backbone.common.ITermMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.aksw.rmltk.model.r2rml.TermMap;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
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

import com.google.common.collect.Sets;

public class TriplesMapProcessorR2rml {

    /** Somewhat custom extension of R2RML which lacks the feature to use a column as a source for language tags.
     *  This constant is not part of the standard R2RML terms but it is supported by this processor.
     *  A property for this will become part of the new RML spec ~ 2023-06-07 */
    public static final Property languageColumn = ResourceFactory.createProperty(R2rmlTerms.uri + "languageColumn");

    protected ITriplesMap triplesMap;
    protected String baseIri;

    /** VarAlloc for generating variables that represent the set of records of a logical source */
    protected VarAlloc sourceVarGen = new VarAlloc("s");

    /** The context for *this* triple maps. Parent contexts are created when there are joins. */
    protected MappingCxt childCxt;

    public TriplesMapProcessorR2rml(ITriplesMap triplesMap, String baseIri) {
        // this(tm, new VarAlloc("v"), HashBiMap.create(), new HashMap<>(), new QuadAcc());
        this.triplesMap = triplesMap;
        this.baseIri = baseIri;
    }

    public static Expr resolveR2rmlReference(String colName) {
        ExprVar column = new ExprVar(colName);
        return column;
    }

    public void initResolvers(MappingCxt cxt) {
        cxt.setReferenceResolver(TriplesMapProcessorR2rml::resolveR2rmlReference);
        cxt.setSourceIdentityResolver(xtriplesMap -> xtriplesMap);
    }

    /**
     *
     * @param processPoms Whether to process predicate object maps. If false then only the subject map will be processed which is useful to process the parent side of a join.
     * @return
     */
    public TriplesMapToSparqlMapping call() {
        // TODO shortcut expansion should already have happened - and doing it in place here is hacky
        R2rmlLib.expandShortcuts(triplesMap);

        Var triplesMapVar = sourceVarGen.allocVar();

        this.childCxt = new MappingCxt(null, triplesMap, triplesMapVar);
        initResolvers(childCxt);

        ISubjectMap sm = triplesMap.getSubjectMap();
        if (sm == null) {
            throw new RuntimeException("SubjectMap was null");
        }

        Node s = allocateVarTracked(childCxt, sm, RR.IRI);
        Objects.requireNonNull(sm, "SubjectMap was null on " + triplesMap);

        Set<? extends IGraphMap> sgms = sm.getGraphMaps();

        for(IPredicateObjectMap pom : triplesMap.getPredicateObjectMaps()) {
            Set<? extends IGraphMap> pogms = pom.getGraphMaps();

            // egms = effective graph maps
            Set<? extends IGraphMap> egms = Sets.union(sgms, pogms);


            // A single graph without a name
            if(egms.isEmpty()) {
                egms = Collections.singleton(null);
            }

            Set<? extends IPredicateMap> pms = pom.getPredicateMaps();
            Set<? extends IObjectMapType> oms = pom.getObjectMaps();

            for(IGraphMap gm : egms) {
                Node g = gm == null ? RR.defaultGraph.asNode() : allocateVarTracked(childCxt, gm, RR.IRI);
                for(IPredicateMap pm : pms) {
                    Node p = allocateVarTracked(childCxt, pm, RR.IRI);
                    for(IObjectMapType om : oms) {
                        if (!om.qualifiesAsRefObjectMap()) {
                            Node o = allocateVarTracked(childCxt, om.asTermMap(), RR.Literal);

                            // Template: creates triples using Quad.defaultGraphNodeGenerated
                            // RDFDataMgr.loadDataset: loads default graph tripls with Quad.defaultGraphIRI
                            // So the output does not match exactly...

                            Quad quad = createQuad(g, s, p, o);
                            childCxt.quadAcc.addQuad(quad);
                        } else {
                            IRefObjectMap rom = om.asRefObjectMap();
                            processRefObjectMap(g, s, p, rom);
                        }
                    }
                }
            }
        }

//        VarExprList vel = new VarExprList();
//        childCxt.getExprDag().getVarToExpr().forEach(vel::add);
        // childCxt.getExprDag().collapse();

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
            ITermMap tm,
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
            ITermMap tm,
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
        } else {
            // Even if the expression already existed it may have not yet been marked as a root
            cxt.getExprDag().getRoots().add(new ExprVar((Var)result));
        }

        return result;
    }

    protected String getLanguageColumn(ITermMap tm) {
        return Optional.ofNullable(tm.getProperty(languageColumn)).map(Statement::getString).orElse(null);
    }

    /**
     * Convert a term map into a corresponding SPARQL expression
     *
     * @param tm
     * @return
     */
    protected Expr termMapToExpr(MappingCxt cxt, ITermMap tm, Resource fallbackTermType) {
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

        // This is an extension of R2RML for a language column - it doesn't seem worth the effort of having this separate
        String langColumn = getLanguageColumn(tm);

        // Infer the effective term type

        if (termTypeNode != null) {
            effectiveTermType = termTypeNode;
        }

        if (constant != null) {
            effectiveTermType = ObjectUtils.requireNullOrEqual(effectiveTermType, R2rmlImporterLib.classifyTermType(constant.asNode()).asNode());
        }

        if (langValue != null || langColumn != null) {
            effectiveTermType = ObjectUtils.requireNullOrEqual(effectiveTermType, RR.Literal.asNode());
        }

        if (effectiveTermType == null && template != null) {
            effectiveTermType = RR.IRI.asNode();
        }

        if (effectiveTermType == null) {
            effectiveTermType = fallbackTermType.asNode();
        }

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
                } else if (langColumn != null) {
                    ExprVar langColumnExprVar = new ExprVar(langColumn);
                    Expr langColumnExpr = resolveColumnReferences(cxt, langColumnExprVar);
                    result = new E_StrLang(columnLikeExpr, langColumnExpr);
                } else {
                    result = R2rmlImporterLib.applyTermType(columnLikeExpr, effectiveTermType, datatypeNode);
                }
            } else {
                throw new RuntimeException("TermMap does neither define rr:template, rr:constant nor rr:column " + tm);
            }
        }

        if (result == null) {
            throw new RuntimeException("Failed to translate term map into an expression. Term map: " + tm);
        }
        // result = postProcessExpr(cxt, result);
        return result;
    }

    /** Transform references from rr:template or rr:column or rr:langColumn */
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

    /**
     * Column like term maps include RML references and custom function invocations.
     * Override this method for RML termMap and references
     */
    protected Expr resolveColumnLikeTermMap(MappingCxt cxt, ITermMap tm, Resource fallbackTermType) {
        Expr result = null;
        String colName = tm.getColumn();
        if(colName != null) {
            Expr rawArg = new ExprVar(colName);
            result = resolveColumnReferences(cxt, rawArg);
        }
        return result;
    }

    protected void processRefObjectMap(Node g, Node s, Node p, IRefObjectMap rom) {
        ITriplesMap parentTm = rom.getParentTriplesMap();
        ISubjectMap parentSm = parentTm.getSubjectMap();

        Var parentVar = sourceVarGen.allocVar();
        MappingCxt parentCxt = new MappingCxt(childCxt, parentTm, parentVar);
        initResolvers(parentCxt);

        Node o = allocateVarTracked(parentCxt, parentSm, RR.IRI);

        Set<? extends IJoinCondition> joinConditions = rom.getJoinConditions();
        ExprList constraints = new ExprList();

        for (IJoinCondition jc : joinConditions) {
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
        Object parentId = getSourceIdentity(parentCxt, parentTm);
        Object childId = getSourceIdentity(childCxt, triplesMap);

        boolean isEliminated = false;

        if (parentId != null && parentId.equals(childId)) {
            // Check whether all constraints are identical when substituting the source variable
            NodeTransform parentToChildSrcVar = n -> parentCxt.getTriplesMapVar().equals(n) ? childCxt.getTriplesMapVar() : n;
            ExprList toCheck = constraints.applyNodeTransform(parentToChildSrcVar);
            boolean isIdentity = toCheck.getList().stream().map(e -> (E_Equals)e).allMatch(e -> Objects.equals(e.getArg1(), e.getArg2()));

            if (isIdentity) {
                // Project on side of the join conditions - at this point the other side has the same expression
                Set<Expr> joinExprs = toCheck.getList().stream().map(e -> (E_Equals)e).map(e -> e.getArg1()).collect(Collectors.toSet());

                // Check if by substitution of all join expressions no further expressions making use of the child/parent source variable remain
                // ExprTransform et = new ExprTransformBase()
                Expr parentSubjectExpr = GenericDag.expand(parentCxt.getExprDag(), parentCxt.getSubjectDefinition().getExpr());
                Expr newParentSubjectExpr = parentSubjectExpr.applyNodeTransform(parentToChildSrcVar);

                Set<Expr> joinCoreDefs = GenericDag.getCoreDefinitions(childCxt.exprDag, joinExprs);
                Set<Expr> childCoreDefs = GenericDag.getCoreDefinitions(childCxt.exprDag);
                Set<Expr> parentCoreDefs = GenericDag.getCoreDefinitions(parentCxt.exprDag).stream()
                        .map(e -> e.applyNodeTransform(parentToChildSrcVar)).collect(Collectors.toCollection(LinkedHashSet::new));

                // Check whether the child subject or parent subject only makes use of definitions that are based on the join definition
                // FIXME Wouldn't we also have to handle the case of a variable predicate?!

                // If either the parent or the child's expression is based only on identity-joining columns then we can eliminate the join
                // https://github.com/Scaseco/r2rml-api-jena/issues/7
                if (joinCoreDefs.containsAll(childCoreDefs) || joinCoreDefs.containsAll(parentCoreDefs)) {
                    isEliminated = true;
                    Var newParentVar = (Var)allocateVarForExpr(childCxt, newParentSubjectExpr);
                    if (newParentVar.isVariable()) {
                        childCxt.termMapToVar.put(rom, newParentVar);
                    }
                    childCxt.getQuadAcc().addQuad(createQuad(g, s, p, newParentVar));
                }
            }
        }

        if (!isEliminated) {
            Quad quad = createQuad(g, s, p, o);
            // parentCxt.getExprDag().collapse();
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
//    protected Object getSourceIdentity(TriplesMap tm) {
//        return tm;
//    }

    protected Quad createQuad(Node g, Node s, Node p, Node o) {
        Node finalG = RR.defaultGraph.asNode().equals(g)
                ? Quad.defaultGraphNodeGenerated
                : g;

        Quad result = Quad.create(finalG, s, p, o);
        return result;
    }

    /**
     * Extension point for resolving RML references.
     * The R2RML processor ignores the source.
     * <b>Important:<b> The returned expression should not be factorized against the cxt's dag. The processor will take care of that.
     *
     * @param source A variable that is bound to the source records and thus represents the source.
     * @param colName A column name or more generally a reference expression string.
     * @return
     */
    protected final Expr referenceToExpr(MappingCxt cxt, String colName) {
        Expr result = Objects
            .requireNonNull(cxt.getReferenceResolver(), "ReferenceResolver not set")
            .apply(colName);

        return result;
        // Expr result = cxt.getReferenceResolver().apply(colName);
        // ExprVar column = new ExprVar(colName);
        // return column;
    }

    protected Object getSourceIdentity(MappingCxt cxt, ITriplesMap tm) {
        Object result = Objects
                .requireNonNull(cxt.getSourceIdentityResolver(), "SourceIdentityResolver not set")
                .apply(tm);

        return result;
    }
}
