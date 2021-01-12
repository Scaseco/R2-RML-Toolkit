package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.r2rml.common.vocab.R2RMLStrings;

public interface RefObjectMap
	extends ObjectMapType
{
	@Iri(R2RMLStrings.parentTriplesMap)
	TriplesMap getParentTriplesMap();
	RefObjectMap setParentTriplesMap(TriplesMap parentTriplesMap);
	
	@Iri(R2RMLStrings.joinCondition)
	Set<JoinCondition> getJoinConditions();
}
