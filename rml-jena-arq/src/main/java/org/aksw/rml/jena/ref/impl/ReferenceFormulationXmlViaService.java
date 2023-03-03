package org.aksw.rml.jena.ref.impl;

import java.util.Arrays;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/** Can also be used for CSV when the CSV processor returns rows as json objects */
public class ReferenceFormulationXmlViaService
    extends ReferenceFormulationViaServiceBase
{
    @Override
    public Expr reference(Var itemVar, String expr) {
        String jsonPath = "http://jsa.aksw.org/fn/xml/path";
        return new E_Function(jsonPath, ExprList.create(Arrays.asList(new ExprVar(itemVar), NodeValue.makeString(expr))));
    }
}
