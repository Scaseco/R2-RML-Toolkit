package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.IJoinCondition;

@ResourceView
public interface JoinConditionRml2
    extends IJoinCondition
{
    @Iri(Rml2Terms.parent)
    @Override String getParent();
    @Override JoinConditionRml2 setParent(String parent);

    @Iri(Rml2Terms.child)
    @Override String getChild();
    @Override JoinConditionRml2 setChild(String child);
}
