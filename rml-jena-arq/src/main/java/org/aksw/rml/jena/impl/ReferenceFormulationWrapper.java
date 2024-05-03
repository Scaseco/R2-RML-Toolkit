package org.aksw.rml.jena.impl;

import org.aksw.rml.model.LogicalSourceRml1;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;

public class ReferenceFormulationWrapper
    implements ReferenceFormulation
{
    protected ReferenceFormulation delegate;

    public ReferenceFormulationWrapper(ReferenceFormulation delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Element source(LogicalSourceRml1 logicalSource, Var sourceVar) {
        return delegate.source(logicalSource, sourceVar);
    }

    @Override
    public Expr reference(Var itemVar, String expr) {
        return delegate.reference(itemVar, expr);
    }
}
