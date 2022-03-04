package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
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
