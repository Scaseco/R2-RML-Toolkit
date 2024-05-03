package org.aksw.rmltk.model.backbone.common;


import java.util.Set;

public interface IRefObjectMap
    extends IObjectMapType
{
    ITriplesMap getParentTriplesMap();
    IRefObjectMap setParentTriplesMap(ITriplesMap parentTriplesMap);

    Set<? extends IJoinCondition> getJoinConditions();
}
