package org.aksw.rml.jena.plugin;

import org.aksw.rml.jena.impl.ReferenceFormulation;

public interface ReferenceFormulationService {
    /** If there is no entry then null must be returned */
    ReferenceFormulation get(String iri);

    default ReferenceFormulation getOrThrow(String iri) {
        ReferenceFormulation result = get(iri);
        if (result == null) {
            throw new IllegalArgumentException("No reference formulation found for " + iri);
        }
        return result;
    }

}
