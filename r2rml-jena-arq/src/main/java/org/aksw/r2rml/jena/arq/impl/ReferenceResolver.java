package org.aksw.r2rml.jena.arq.impl;

import org.apache.jena.sparql.expr.Expr;

public interface ReferenceResolver {
    Expr resolve(String str);
}
