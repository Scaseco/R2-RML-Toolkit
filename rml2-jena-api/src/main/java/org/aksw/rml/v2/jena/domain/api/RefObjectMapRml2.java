package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;

@ResourceView
public interface RefObjectMapRml2
    extends IRefObjectMap, ObjectMapTypeRml2
{
    @Iri(Rml2Terms.parentTriplesMap)
    @Override TriplesMapRml2 getParentTriplesMap();
    @Override RefObjectMapRml2 setParentTriplesMap(ITriplesMap parentTriplesMap);

    @Iri(Rml2Terms.joinCondition)
    @Override
    Set<JoinConditionRml2> getJoinConditions();
}
