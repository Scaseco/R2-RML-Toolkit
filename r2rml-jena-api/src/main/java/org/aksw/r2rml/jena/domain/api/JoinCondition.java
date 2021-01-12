package org.aksw.r2rml.jena.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2RMLStrings;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface JoinCondition 
	extends Resource
{
	@Iri(R2RMLStrings.parent)
	String getParent();
	JoinCondition setParent();
	
	@Iri(R2RMLStrings.child)
	String getChild();
	JoinCondition setChild();
}
