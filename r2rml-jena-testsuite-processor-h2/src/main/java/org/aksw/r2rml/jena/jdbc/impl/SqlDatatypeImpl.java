package org.aksw.r2rml.jena.jdbc.impl;

import java.util.function.Function;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;

public class SqlDatatypeImpl
	implements SqlDatatype
{
	protected int sqlType;
	protected Class<?> javaClass;
	protected RDFDatatype rdfDatatype;
	
	protected Function<Object, Object> compatibilizer;
	protected Function<Object, String> lexicalFormizer;
	
	protected UserDefinedFunctionDefinition convertSqlToRdf;
	
	public SqlDatatypeImpl(int sqlType, Class<?> javaClass, RDFDatatype rdfDatatype) {
		this(sqlType, javaClass, rdfDatatype, null, null, null);
	}
	

	public SqlDatatypeImpl(int sqlType, Class<?> javaClass, RDFDatatype rdfDatatype,
			Function<Object, Object> compatibilizer,
			Function<Object, String> lexicalFormizer,
			UserDefinedFunctionDefinition convertSqlToRdf) {
		super();
		this.sqlType = sqlType;
		this.javaClass = javaClass;
		this.rdfDatatype = rdfDatatype;
		this.compatibilizer = compatibilizer;
		this.lexicalFormizer = lexicalFormizer;
		this.convertSqlToRdf = convertSqlToRdf;
	}




	@Override
	public int getSqlType() {
		return sqlType;
	}

	@Override
	public Class<?> getJavaClass() {
		return javaClass;
	}

	@Override
	public RDFDatatype getRdfDatatype() {
		return rdfDatatype;
	}

	@Override
	public String toString() {
		return "SqlDatatypeImpl [sqlType=" + sqlType + ", javaClass=" + javaClass + ", rdfDatatype=" + rdfDatatype
				+ "]";
	}

	@Override
	public UserDefinedFunctionDefinition convertSqlToRdf() {
		return convertSqlToRdf;
	}

	@Override
	public Function<Object, Object> getCompatibilizer() {
		return compatibilizer;
	}

	@Override
	public Function<Object, String> getLexicalFormizer() {
		return lexicalFormizer;
	}
}
