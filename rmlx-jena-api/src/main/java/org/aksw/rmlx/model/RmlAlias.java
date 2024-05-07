package org.aksw.rmlx.model;

import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface RmlAlias
    extends RmlDefinition
{
//    /**
//     * The name of an alias for the given reference (expression) string
//     */
//    @Iri("http://www.w3.org/2000/01/rdf-schema#label")
//    String getLabel();
    @Override
    RmlAlias setLabel(String label);
//
//    /**
//     * An RML reference (expression) string which to alias by the label
//     */
//    @Iri(NorseRmlTerms.definition)
//    String getReference();
    @Override
    RmlAlias setDefinition(String reference);
}
