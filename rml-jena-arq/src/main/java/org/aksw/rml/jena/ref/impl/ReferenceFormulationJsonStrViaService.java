package org.aksw.rml.jena.ref.impl;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;

/**
 * Variant of {@link ReferenceFormulationJsonViaService} that converts every
 * reference resolution result as a string.
 * Used for RML2 test cases that do not demand a natural mapping of JSON types to RDF types.
 */
public class ReferenceFormulationJsonStrViaService
    extends ReferenceFormulationJsonViaService
{
    @Override
    public Expr reference(Var itemVar, String expr) {
        Expr tmp = super.reference(itemVar, expr);
        return new E_Str(tmp);
    }
}
