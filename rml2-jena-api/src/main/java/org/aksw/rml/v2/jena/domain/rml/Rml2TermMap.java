package org.aksw.rml.v2.jena.domain.rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.ITermMap;

@ResourceView
public interface Rml2TermMap
    extends ITermMap
{
    @Iri(Rml2Terms.reference)
    String getReference();
    Rml2TermMap setReference(String reference);
}
