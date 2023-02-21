package org.aksw.rml.jena.impl;

import java.util.Arrays;

import org.aksw.fno.model.Fno;
import org.aksw.rml.model.LogicalSource;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

public class ReferenceFormulationCsvViaService
    implements ReferenceFormulation
{
    @Override
    public Element source(LogicalSource logicalSource, Var sourceVar) {
        BasicPattern bgp = new BasicPattern();
        // Only add the immediatly triples
        logicalSource.listProperties().mapWith(stmt -> stmt.asTriple()).forEach(bgp::add);
        // GraphUtil.findAll(logicalSource.getModel().getGraph()).forEach(bgp::add);
        bgp.add(Triple.create(logicalSource.asNode(), Fno.returns.asNode(), sourceVar));
        ElementService result = new ElementService(SparqlX_Rml_Terms.RML_SOURCE_SERVICE_IRI, new ElementTriplesBlock(bgp));
        return result;
    }

    @Override
    public Expr reference(Var itemVar, String expr) {
        String jsonPath = "http://jsa.aksw.org/fn/json/path";
        return new E_Function(jsonPath, ExprList.create(Arrays.asList(new ExprVar(itemVar), NodeValue.makeString("$." + expr))));
    }
}
