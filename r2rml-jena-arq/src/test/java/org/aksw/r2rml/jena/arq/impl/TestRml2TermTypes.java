package org.aksw.r2rml.jena.arq.impl;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.junit.Assert;
import org.junit.Test;

/** rml:URI is a synonym of rml:IRI in RML-Core (test case RMLTC0027a, kg-construct/rml-core). */
public class TestRml2TermTypes {

    static Expr termType(String localName) {
        return R2rmlImporterLib.applyTermType(new ExprVar("x"),
                NodeFactory.createURI("http://w3id.org/rml/" + localName), null);
    }

    @Test
    public void testIri() {
        Assert.assertTrue(termType("IRI") instanceof E_IRI);
    }

    @Test
    public void testUri() {
        Assert.assertTrue(termType("URI") instanceof E_IRI);
    }
}
