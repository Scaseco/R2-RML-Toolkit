package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;

@ResourceView
public interface RefObjectMap
	extends ObjectMapType
{
	@Iri(R2rmlTerms.parentTriplesMap)
	TriplesMap getParentTriplesMap();
	RefObjectMap setParentTriplesMap(TriplesMap parentTriplesMap);
	
	@Iri(R2rmlTerms.joinCondition)
	Set<JoinCondition> getJoinConditions();
}
