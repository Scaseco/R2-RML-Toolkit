package org.aksw.rmlx.model;

import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface RmlBind extends RmlDefinition {
//    /**
//     * The name of an alias for the given SPARQL expression string
//     */
//    @Iri("http://www.w3.org/2000/01/rdf-schema#label")
//    String getLabel();
    @Override
    RmlBind setLabel(String label);

//
//    /**
//     * A SPARQL expression string which to alias by the label
//     */
//    @Iri(NorseRmlTerms.definition)
//    String getDefinition();
    @Override
    RmlBind setDefinition(String reference);
}
