package org.aksw.r2rml.jena.arq.impl;

import org.aksw.r2rml.jena.domain.api.RefObjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.ElementBind;

public class JoinDeclaration {
    protected MappingCxt parentCxt; // Owns a reference to the child cxt
    // protected TriplesMap childTriplesMap;
    protected TermMap predicateMap; // Remove?
    protected RefObjectMap refObjectMap; // The child's refObjectMap

    protected Quad quad;
    protected ExprList conditionExprs;

    public JoinDeclaration(MappingCxt parentCxt, TermMap predicateMap, RefObjectMap refObjectMap, Quad quad, ExprList conditionExprs) {
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

    public TriplesMap getParentTriplesMap() {
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

    public TriplesMap getChildTriplesMap() {
        return getChildCxt().getTriplesMap();
    }

    public TermMap getPredicateMap() {
        return predicateMap;
    }

    public RefObjectMap getRefObjectMap() {
        return refObjectMap;
    }

    public Quad getQuad() {
        return quad;
    }

    public ExprList getConditionExprs() {
        return conditionExprs;
    }
}