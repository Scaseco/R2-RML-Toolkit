package org.aksw.r2rml.jena.jdbc.impl;

import java.util.function.Function;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;

public interface SqlDatatype {
	int getSqlType();
	Class<?> getJavaClass(); // e.g. java.sql.Timestamp for java.sql.Types.Timstamp
	RDFDatatype getRdfDatatype();
	
	
	// Convert an SQL object to one that is compatible with the RDFDatatype
	// Useful to avoids conversion via lexical forms
	// May be null
	Function<Object, Object> getCompatibilizer();
	
	// Convert an SQL object to a lexical form that is compatible with the RDF datatype
	// If neither lexical formizer nor compatibilizer is specified NodeFactory.createLiteralFromValue(object, rdfDatatype) is used
	// If both are specified then the compatibilizer is typically preferred
	// May be null
	Function<Object, String> getLexicalFormizer();
	
	// Algebra transformation associated with the datatype to convert an sql value to rdf
	UserDefinedFunctionDefinition convertSqlToRdf();
	
	
	// A single-valued expression that transforms a raw natural RDF value into
	// the effective natural value
	// e.g. REPLACE(?x, ' ', 'T') for replacing whitespaces on sql DateTime strings so
	// RDFDatatype.xsdDateTime can handle it.
	// Entry<Var, Expr> getTransformExpression();
	
	// Preprocess an object such that it becomes suitable for use in
	// NodeFactory.createLiteralByValue(convertedObject, this.getRdfDatatype())
	// Object convertForRdf(Object);
	// Node convertToNode(Object o);
	
	
	default Function<Object, Node> getNodeMapper() {
		RDFDatatype rdfDatatype = getRdfDatatype();
		Function<Object, Object> compatibilizer = getCompatibilizer();
		Function<Object, String> lexicalFormizer = getLexicalFormizer();
		
		Function<Object, Node> result;
		if (compatibilizer != null) {
			result = obj -> {
				Object compat = compatibilizer.apply(obj);
//				System.out.println("GOT: " + compat);
//				System.out.println(obj.getClass());
//				System.out.println(rdfDatatype.getURI());
//				System.out.println(rdfDatatype.isValidValue(compat));

				Node r = NodeFactory.createLiteralByValue(compat, rdfDatatype);
				return r;
			};
		} else if (lexicalFormizer != null) {
			result = obj -> {
				String lexicalForm = lexicalFormizer.apply(obj);
				Node r = NodeFactory.createLiteral(lexicalForm, rdfDatatype);
				return r;
			};			
		} else {
			result = obj -> {
//				System.out.println("GOT: " + obj);
//				System.out.println(obj.getClass());
//				System.out.println(rdfDatatype.getURI());
//				System.out.println(rdfDatatype.isValidValue(obj));
				Node r = NodeFactory.createLiteralByValue(obj, rdfDatatype);
				return r;
			};
		}
		
		return result;
	}	

}
