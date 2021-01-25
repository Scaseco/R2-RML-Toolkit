package org.aksw.r2rml.jena.jdbc.impl;

import org.apache.jena.datatypes.RDFDatatype;

public interface SqlDatatype {
	int getSqlType();
	Class<?> getJavaClass();
	RDFDatatype getRdfDatatype();
	
	// A single-valued expression that transforms a raw natural RDF value into
	// the effective natural value
	// e.g. REPLACE(?x, ' ', 'T') for replacing whitespaces on sql DateTime strings so
	// RDFDatatype.xsdDateTime can handle it.
	// Entry<Var, Expr> getTransformExpression();
	
	// Preprocess an object such that it becomes suitable for use in
	// NodeFactory.createLiteralByValue(convertedObject, this.getRdfDatatype())
	// Object convertForRdf(Object);
	// Node convertToNode(Object o);
}
