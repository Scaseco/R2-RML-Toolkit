package org.aksw.rml.jena.common;


import java.util.Set;

public interface RefObjectMap
    extends ObjectMapType
{
    TriplesMap getParentTriplesMap();
    RefObjectMap setParentTriplesMap(TriplesMap parentTriplesMap);

    Set<JoinCondition> getJoinConditions();
}
