package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;

@ResourceView
public interface RefObjectMap
    extends ObjectMapType
{
    @Iri(Rml2Terms.parentTriplesMap)
    TriplesMap getParentTriplesMap();
    RefObjectMap setParentTriplesMap(TriplesMap parentTriplesMap);

    @Iri(Rml2Terms.joinCondition)
    Set<JoinCondition> getJoinConditions();
}
