package org.aksw.r2rml.jena.arq.impl;

import org.aksw.r2rml.jena.domain.api.RefObjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;

public class JoinDeclaration {
    protected TriplesMap childTriplesMap;
    protected TermMap predicateMap;
    protected RefObjectMap refObjectMap;

    protected Var parentVar;
    protected Var childVar;
    protected Quad quad;
    protected ExprList conditionExprs;

    public JoinDeclaration(TriplesMap childTriplesMap, Var childVar, TermMap predicateMap, RefObjectMap refObjectMap,
            Var parentVar, Quad quad, ExprList conditionExprs) {
        super();
        this.childTriplesMap = childTriplesMap;
        this.childVar = childVar;
        this.predicateMap = predicateMap;
        this.refObjectMap = refObjectMap;
        this.parentVar = parentVar;
        this.quad = quad;
        this.conditionExprs = conditionExprs;
    }

    /** Convenience shortcut */
    public TriplesMap getParentTriplesMap() {
        return refObjectMap.getParentTriplesMap();
    }

    public TriplesMap getChildTriplesMap() {
        return childTriplesMap;
    }

    public TermMap getPredicateMap() {
        return predicateMap;
    }

    public RefObjectMap getRefObjectMap() {
        return refObjectMap;
    }

    public Var getParentVar() {
        return parentVar;
    }

    public Var getChildVar() {
        return childVar;
    }

    public Quad getQuad() {
        return quad;
    }

    public ExprList getConditionExprs() {
        return conditionExprs;
    }
}