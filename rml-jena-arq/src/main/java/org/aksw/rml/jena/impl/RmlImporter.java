package org.aksw.rml.jena.impl;

import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.rdf.model.Model;

public class RmlImporter {
    public static TriplesMapToSparqlMapping read(TriplesMap tm, String baseIri, Model fnmlModel) {
        TriplesMapToSparqlMapping result = new TriplesMapProcessorRml(tm, baseIri, fnmlModel).call();
        return result;
    }
}
