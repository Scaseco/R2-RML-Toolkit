package org.aksw.r2rml.jena.arq.impl;

import org.aksw.rmltk.model.r2rml.TriplesMap;

/**
 * Main class for importing R2RML documents into TriplesMapToSparqlMappings
 */
public class R2rmlImporter {
    public TriplesMapToSparqlMapping read(TriplesMap tm, String baseIri) {
        TriplesMapToSparqlMapping result = new TriplesMapProcessorR2rml(tm, baseIri).call();
        return result;
    }
}
