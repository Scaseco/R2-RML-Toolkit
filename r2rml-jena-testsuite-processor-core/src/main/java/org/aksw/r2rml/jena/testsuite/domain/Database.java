package org.aksw.r2rml.jena.testsuite.domain;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.Namespace;
import org.aksw.jena_sparql_api.mapper.annotation.Namespaces;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@Namespaces({
	@Namespace(prefix = "dcterms", value = "http://purl.org/dc/elements/1.1/"),
	@Namespace(prefix = "rdb2rdftest", value = "http://purl.org/NET/rdb2rdf-test#")
})
public interface Database
	extends Resource
{
	@IriNs("dcterms")
	String getTitle();
	Database setTitle(String title);
	
	@IriNs("dcterms")
	String getIdentifier();
	Database setIdentifier(String identifier);

	@IriNs("rdb2rdftest")
	String getSqlScriptFile();
	Database setSqlScriptFile(String sqlScriptFile);

	@Iri("rdb2rdftest:database")
	@Inverse
	Set<R2rmlTestCase> getRelatedTestCases();

	
	//	rdb2rdftest:relatedTestCase <dg0003>, <tc0003a>, <tc0003b>, <tc0003c> ;
}
