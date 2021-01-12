package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2RMLStrings;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PredicateObjectMap
	extends MappingComponent
{
	@Iri(R2RMLStrings.graphMap)
	Set<GraphMap> getGraphMaps();

	@Iri(R2RMLStrings.predicate)
	Set<Resource> getPredicates();

	@Iri(R2RMLStrings.predicateMap)
	Set<PredicateMap> getPredicateMaps();

	@Iri(R2RMLStrings.objectMap)
	Set<ObjectMap> getObjectMaps();

	/** Allocate and add a fresh object map */
	default ObjectMap addNewObjectMap() {
		ObjectMap result = getModel().createResource().as(ObjectMap.class);
		getObjectMaps().add(result);
		return result;
	}


	default PredicateObjectMap addPredicate(String uri) {
		return addPredicate(NodeFactory.createURI(uri));
	}

	default PredicateObjectMap addPredicate(Node node) {
		return addPredicate(getModel().wrapAsResource(node));
	}

	default PredicateObjectMap addPredicate(Resource resource) {
		getPredicates().add(resource);
		return this;
	}

//	Resource getPredicate();
//	PredicateObjectMap setPredicate(Resource predicate);
//
//	TermMap getPredicateMap();
//	PredicateObjectMap setPredicateMap(TermMap termMap);
//	
//	TermMap getObjectMap();
//	PredicateObjectMap setObjectMap(TermMap termMap);
}
