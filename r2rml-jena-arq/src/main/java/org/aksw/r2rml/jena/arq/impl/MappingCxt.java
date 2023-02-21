package org.aksw.r2rml.jena.arq.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.r2rml.jena.domain.api.TermSpec;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.modify.request.QuadAcc;

/**
 * This class captures the state for mapping TriplesMap to SPARQL elements.
 */
public class MappingCxt {
    protected MappingCxt parentCxt;

    protected TriplesMap triplesMap;
    protected Var triplesMapVar;

    protected BiMap<Var, Expr> varToExpr = HashBiMap.create();
    protected Map<TermSpec, Var> termMapToVar = new HashMap<>();

    // Accumulator for generated quads
    protected QuadAcc quadAcc = new QuadAcc();

    // TODO Allow customization of variable allocation
    protected VarAlloc varGen = new VarAlloc("v");

    protected List<JoinDeclaration> joins = new ArrayList<>();

    public MappingCxt(MappingCxt parentCxt, TriplesMap triplesMap, Var triplesMapVar) {
        super();
        this.parentCxt = parentCxt;
        this.triplesMap = triplesMap;
        this.triplesMapVar = triplesMapVar;
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

    public BiMap<Var, Expr> getVarToExpr() {
        return varToExpr;
    }

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