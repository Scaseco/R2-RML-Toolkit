package org.aksw.r2rml.jena.arq.impl;

import org.aksw.r2rml.jena.domain.api.TriplesMap;

public interface TriplesMapProcessorFactory {
    TriplesMapToSparqlMapping process(TriplesMap triplesMap, boolean processPoms);
}
