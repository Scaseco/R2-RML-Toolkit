package org.aksw.rml.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.aksw.rmltk.model.r2rml.JoinCondition;

@ResourceView
public interface RefObjectMapRml1
    extends IRefObjectMap, ObjectMapTypeRml1
{
    @Iri(R2rmlTerms.parentTriplesMap)
    @Override TriplesMapRml1 getParentTriplesMap();
    @Override RefObjectMapRml1 setParentTriplesMap(ITriplesMap parentTriplesMap);

    @Iri(R2rmlTerms.joinCondition)
    @Override Set<JoinCondition> getJoinConditions();
}
