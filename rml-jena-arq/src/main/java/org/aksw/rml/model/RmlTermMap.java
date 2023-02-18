package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.r2rml.jena.domain.api.TermMap;

public interface RmlTermMap
    extends TermMap
{
    @Iri(RmlTerms.reference)
    String getReference();
    RmlTermMap setReference(String reference);
}
