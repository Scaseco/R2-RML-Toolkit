package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.r2rml.TermMap;

@ResourceView
public interface TermMapRml1
    extends TermMap
{
    @Iri(RmlTerms.reference)
    String getReference();
    TermMapRml1 setReference(String reference);
}
