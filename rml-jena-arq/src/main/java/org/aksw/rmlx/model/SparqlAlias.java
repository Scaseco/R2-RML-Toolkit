package org.aksw.rmlx.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.model.RmlTerms;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface SparqlAlias
    extends Resource
{
    /**
     * The name of an alias for the given reference (expression) string
     */
    @Iri("http://www.w3.org/2000/01/rdf-schema#label")
    String getLabel();
    SparqlAlias setLabel(String label);

    /**
     * An RML reference (expression) string which to alias by the label
     */
    @Iri(RmlTerms.reference)
    String getReference();
    SparqlAlias setReference(String reference);
}
