package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2RMLStrings;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface SubjectMap
	extends TermMap, HasGraphMap
{	

	/**
	 * Return a set view (never null) of resources specified via rr:class
	 * 
	 * @return
	 */
	@Iri(R2RMLStrings.xclass)
	Set<Resource> getClasses();
}
