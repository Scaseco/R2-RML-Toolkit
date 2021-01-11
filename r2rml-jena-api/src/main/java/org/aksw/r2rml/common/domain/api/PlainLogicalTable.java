package org.aksw.r2rml.common.domain.api;

/**
 * Interface for logical tables suitable for use also
 * with non-RDF implementations.
 * 
 * @author Claus Stadler
 *
 */
public interface PlainLogicalTable {
	String getTableName();
	PlainLogicalTable setTableName(String tableName);
	
	String getSqlQuery();
	PlainLogicalTable setSqlQuery(String queryString);
}
