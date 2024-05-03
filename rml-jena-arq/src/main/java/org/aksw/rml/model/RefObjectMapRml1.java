package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;

@ResourceView
public interface RefObjectMapRml1
    extends IRefObjectMap
{
    // @Iri(R2rmlTerms.parentTriplesMap)
    @Override TriplesMapRml1 getParentTriplesMap();
    @Override RefObjectMapRml1 setParentTriplesMap(ITriplesMap parentTriplesMap);

    // @Iri(R2rmlTerms.joinCondition)
    // @Override Set<JoinCondition> getJoinConditions();
}
