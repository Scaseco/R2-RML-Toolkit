package org.aksw.rml.v2.jena.domain.rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rml.v2.jena.domain.api.TermMap;

@ResourceView
public interface RmlTermMap
    extends TermMap
{
    @Iri(Rml2Terms.reference)
    String getReference();
    RmlTermMap setReference(String reference);
}
