package org.aksw.r2rml.jena.arq.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.r2rml.jena.domain.api.TermSpec;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.ElementBind;

/**
 * This class captures the state for mapping a TriplesMap to SPARQL elements.
 */
public class MappingCxt {
    /** Reference to the triples map that acts as the _child_ of rr:joins */
    protected MappingCxt parentCxt;

    protected TriplesMap triplesMap;
    protected Var triplesMapVar;

    /** Data structure to factor out common subexpressions eagerly */
    protected GenericDag<Expr, Var> exprDag;
    // protected BiMap<Var, Expr> varToExpr = HashBiMap.create();
    protected Map<TermSpec, Var> termMapToVar = new HashMap<>();

    // Accumulator for generated quads
    protected QuadAcc quadAcc = new QuadAcc();

    // TODO Allow customization of variable allocation
    protected VarAlloc varGen;

    protected List<JoinDeclaration> joins = new ArrayList<>();


    /** The reference resolver can be set after context creation. In the case of RML it needs information from the context. */
    protected Function<String, Expr> referenceResolver = null;
    protected Function<TriplesMap, Object> sourceIdentityResolver = null;

    public MappingCxt(MappingCxt parentCxt, TriplesMap triplesMap, Var triplesMapVar) {
        super();
        this.parentCxt = parentCxt;
        this.triplesMap = triplesMap;
        this.triplesMapVar = triplesMapVar;

        String baseExprVar = parentCxt == null ? "v" : triplesMapVar.getName() + "_v";
        this.varGen = new VarAlloc(baseExprVar);
        this.exprDag = new GenericDag<>(ExprUtils.getExprOps(), varGen::allocVar, null);
    }

    public Function<String, Expr> getReferenceResolver() {
        return referenceResolver;
    }

    public void setReferenceResolver(Function<String, Expr> referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public Function<TriplesMap, Object> getSourceIdentityResolver() {
        return sourceIdentityResolver;
    }

    public void setSourceIdentityResolver(Function<TriplesMap, Object> sourceIdentityResolver) {
        this.sourceIdentityResolver = sourceIdentityResolver;
    }

    public Var getSubjectVar() {
        MappingCxt cxt = this; // Make this a static util function?
        TriplesMap tm = cxt.getTriplesMap();
        Var result = cxt.getTermMapToVar().get(tm.getSubjectMap());
        return result;
    }

    public ElementBind getSubjectDefinition() {
        MappingCxt cxt = this; // Make this a static util function?
        Var subjectVar = getSubjectVar();
        Expr expr = cxt.getExprDag().getExpr(subjectVar);
        return new ElementBind(subjectVar, expr);
    }

    public TriplesMap getTriplesMap() {
        return triplesMap;
    }

    public MappingCxt getParentCxt() {
        return parentCxt;
    }

    public Var getTriplesMapVar() {
        return triplesMapVar;
    }

    public GenericDag<Expr, Var> getExprDag() {
        return exprDag;
    }

//    public BiMap<Var, Expr> getVarToExpr() {
//        return exprDag.getVarToExpr();
//    }

    public Map<TermSpec, Var> getTermMapToVar() {
        return termMapToVar;
    }

    public QuadAcc getQuadAcc() {
        return quadAcc;
    }

    public VarAlloc getVarGen() {
        return varGen;
    }

    public List<JoinDeclaration> getJoins() {
        return joins;
    }
}
