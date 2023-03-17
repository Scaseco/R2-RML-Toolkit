package org.aksw.rml.jena.ref.impl;

import java.util.Arrays;

import org.aksw.jena_sparql_api.sparql.ext.json.SparqlX_Json_Terms;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

public class ReferenceFormulationCsvViaService
    extends ReferenceFormulationJsonViaService {
    @Override
    public Expr reference(Var itemVar, String expr) {
        return new E_Function(SparqlX_Json_Terms.get, ExprList.create(Arrays.asList(new ExprVar(itemVar), NodeValue.makeString(expr))));
    }
}
