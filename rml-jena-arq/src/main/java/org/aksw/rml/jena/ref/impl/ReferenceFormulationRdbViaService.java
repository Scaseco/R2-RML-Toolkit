package org.aksw.rml.jena.ref.impl;

import java.util.Arrays;

import org.aksw.jena_sparql_api.sparql.ext.json.NorseJsonTerms;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/** Can also be used for CSV when the CSV processor returns rows as json objects */
public class ReferenceFormulationRdbViaService
    extends ReferenceFormulationViaServiceBase
{
    private static final ReferenceFormulationRdbViaService INSTANCE = new ReferenceFormulationRdbViaService();

    public static ReferenceFormulationRdbViaService getInstance() {
        return INSTANCE;
    }

    @Override
    public Expr reference(Var itemVar, String expr) {
        return new E_Function(NorseJsonTerms.get, ExprList.create(Arrays.asList(new ExprVar(itemVar), NodeValue.makeString(expr))));
    }
}
