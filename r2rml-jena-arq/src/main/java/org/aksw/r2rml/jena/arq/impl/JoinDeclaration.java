package org.aksw.r2rml.jena.arq.impl;

import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITermMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.ElementBind;

public class JoinDeclaration {
    protected MappingCxt parentCxt; // Owns a reference to the child cxt
    // protected TriplesMap childTriplesMap;
    protected ITermMap predicateMap; // Remove?
    protected IRefObjectMap refObjectMap; // The child's refObjectMap

    protected Quad quad;
    protected ExprList conditionExprs;

    public JoinDeclaration(MappingCxt parentCxt, ITermMap predicateMap, IRefObjectMap refObjectMap, Quad quad, ExprList conditionExprs) {
        super();
        this.parentCxt = parentCxt;
        this.predicateMap = predicateMap;
        this.refObjectMap = refObjectMap;
        this.quad = quad;
        this.conditionExprs = conditionExprs;
    }

    /* Convenience shortcuts */

    public MappingCxt getParentCxt() {
        return parentCxt;
    }

    public ITriplesMap getParentTriplesMap() {
        return getParentCxt().getTriplesMap(); // same as refObjectMap.getParentTriplesMap();
    }

    public ElementBind getParentSubjectDefinition() {
        return getParentCxt().getSubjectDefinition();
    }

    public Var getParentVar() {
        return getParentCxt().getTriplesMapVar();
    }

    public MappingCxt getChildCxt() {
        return getParentCxt().getParentCxt();
    }

    public Var getChildVar() {
        return getChildCxt().getTriplesMapVar();
    }

    public ElementBind getChildSubjectDefinition() {
        return getChildCxt().getSubjectDefinition();
    }

    public ITriplesMap getChildTriplesMap() {
        return getChildCxt().getTriplesMap();
    }

    public ITermMap getPredicateMap() {
        return predicateMap;
    }

    public IRefObjectMap getRefObjectMap() {
        return refObjectMap;
    }

    public Quad getQuad() {
        return quad;
    }

    public ExprList getConditionExprs() {
        return conditionExprs;
    }
}
