package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface JoinCondition
    extends Resource
{
    @Iri(Rml2Terms.parent)
    String getParent();
    JoinCondition setParent(String parent);

    @Iri(Rml2Terms.child)
    String getChild();
    JoinCondition setChild(String child);
}
