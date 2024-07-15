package org.aksw.rml.jena.ref.impl;

import java.util.Arrays;

import org.aksw.jenax.norse.term.json.NorseTermsJson;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/** Can also be used for CSV when the CSV processor returns rows as json objects */
public class ReferenceFormulationJsonViaService
    extends ReferenceFormulationViaServiceBase
{
    @Override
    public Expr reference(Var itemVar, String expr) {
        String column;
        if (expr.startsWith("$")) {
            column = expr;
        } else {
            column = "$['" + expr.replaceAll("'", "\\'") + "']";
        }
        return new E_Function(
                NorseTermsJson.path,
                ExprList.create(Arrays.asList(
                        new ExprVar(itemVar),
                        NodeValue.makeString(column))
                )
        );
    }
}
