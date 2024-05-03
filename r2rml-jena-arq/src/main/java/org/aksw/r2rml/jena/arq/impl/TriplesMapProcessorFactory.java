package org.aksw.r2rml.jena.arq.impl;

import org.aksw.rmltk.model.r2rml.TriplesMap;

public interface TriplesMapProcessorFactory {
    TriplesMapToSparqlMapping process(TriplesMap triplesMap, boolean processPoms);
}
