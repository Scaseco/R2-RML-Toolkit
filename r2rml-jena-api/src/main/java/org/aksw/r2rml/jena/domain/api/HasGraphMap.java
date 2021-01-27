package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.apache.jena.rdf.model.Resource;

/**
 * Interface common to SubjectMap and PredicateObjectMap
 * 
 * @author raven
 *
 */
@ResourceView
public interface HasGraphMap
	extends Resource
{
	@Iri(R2rmlTerms.graphMap)
	Set<GraphMap> getGraphMaps();


	@Iri(R2rmlTerms.graph)
	Set<Resource> getGraphs();

	default GraphMap addNewGraphMap() {
		GraphMap result = getModel().createResource().as(GraphMap.class);
		getGraphMaps().add(result);
		return result;
	}
}
