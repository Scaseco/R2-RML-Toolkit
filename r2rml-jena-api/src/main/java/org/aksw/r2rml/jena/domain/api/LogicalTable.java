package org.aksw.r2rml.jena.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2RMLStrings;

/**
 * Interface for RDF-based logical tables.
 * 
 * As this denotes the same information as in the more basic
 * {@link org.aksw.obda.domain.api.LogicalTable}, deriving from it should be
 * safe.
 * 
 *  
 * 
 * @author raven Apr 1, 2018
 *
 */
@ResourceView
public interface LogicalTable
	extends MappingComponent
{
	@Iri(R2RMLStrings.tableName)
	String getTableName();
	LogicalTable setTableName(String tableName);
	
	@Iri(R2RMLStrings.sqlQuery)
	String getSqlQuery();
	LogicalTable setSqlQuery(String queryString);
	
	default boolean hasTableName() {
		return getTableName() != null;
	}

	default boolean hasSqlQuery() {
		return getSqlQuery() != null;
	}
}
