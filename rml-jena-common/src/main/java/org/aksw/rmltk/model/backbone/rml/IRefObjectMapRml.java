package org.aksw.rmltk.model.backbone.rml;


import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;

public interface IRefObjectMapRml
    extends IRefObjectMap
{
    @Override ITriplesMapRml getParentTriplesMap();
    @Override IRefObjectMapRml setParentTriplesMap(ITriplesMap parentTriplesMap);

    // Set<? extends IJoinCondition> getJoinConditions();
}
