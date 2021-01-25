package org.aksw.r2rml.jena.jdbc.impl;

import org.apache.jena.datatypes.RDFDatatype;

public class SqlDatatypeImpl
	implements SqlDatatype
{
	protected int sqlType;
	protected Class<?> javaClass;
	protected RDFDatatype rdfDatatype;
	
	public SqlDatatypeImpl(int sqlType, Class<?> javaClass, RDFDatatype rdfDatatype) {
		super();
		this.sqlType = sqlType;
		this.javaClass = javaClass;
		this.rdfDatatype = rdfDatatype;
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
}
