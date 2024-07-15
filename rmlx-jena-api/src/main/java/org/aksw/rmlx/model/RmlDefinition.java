package org.aksw.rmlx.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

public interface RmlDefinition
    extends Resource
{
    @Iri("http://www.w3.org/2000/01/rdf-schema#label")
    String getLabel();
    RmlDefinition setLabel(String label);

    /**
     * An RML reference (expression) string which to alias by the label
     */
    @Iri(RmlXTerms.definition)
    String getDefinition();
    RmlDefinition setDefinition(String definition);
}
