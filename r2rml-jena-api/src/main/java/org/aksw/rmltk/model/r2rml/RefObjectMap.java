package org.aksw.rmltk.model.r2rml;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;

@ResourceView
public interface RefObjectMap
    extends IRefObjectMap, ObjectMapType
{
    @Iri(R2rmlTerms.parentTriplesMap)
    @Override TriplesMap getParentTriplesMap();
    @Override RefObjectMap setParentTriplesMap(ITriplesMap parentTriplesMap);

    @Iri(R2rmlTerms.joinCondition)
    @Override Set<JoinCondition> getJoinConditions();
}
