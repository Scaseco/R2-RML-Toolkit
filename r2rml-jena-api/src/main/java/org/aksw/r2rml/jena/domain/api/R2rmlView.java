package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2RMLStrings;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface R2rmlView
	extends LogicalTable
{
	@Iri(R2RMLStrings.sqlQuery)
	String getSqlQuery();
	R2rmlView setSqlQuery(String queryString);	

	@Iri(R2RMLStrings.sqlVersion)
	Set<Resource> getSqlVersions();

	/**
	 * Convenience view of the resource IRIs as strings
	 * 
	 * @return
	 */
	@Iri(R2RMLStrings.sqlVersion)
	@IriType
	Set<String> getSqlVersionIris();
}
