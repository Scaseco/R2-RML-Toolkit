package org.aksw.rmltk.model.r2rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.IJoinCondition;

@ResourceView
public interface JoinCondition
    extends IJoinCondition
{
    @Iri(R2rmlTerms.parent)
    @Override String getParent();
    @Override JoinCondition setParent(String parent);

    @Iri(R2rmlTerms.child)
    @Override String getChild();
    @Override JoinCondition setChild(String child);
}
