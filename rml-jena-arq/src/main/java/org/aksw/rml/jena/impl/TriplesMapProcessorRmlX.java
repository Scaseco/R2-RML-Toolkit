package org.aksw.rml.jena.impl;

import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.apache.jena.rdf.model.Model;

/** Extension of the {@link TriplesMapProcessorRml} to support aliases */
public class TriplesMapProcessorRmlX
    extends TriplesMapProcessorRml
{
    public TriplesMapProcessorRmlX(TriplesMap triplesMap, String baseIri, Model fnmlModel,
            ReferenceFormulationRegistry registry) {
        super(triplesMap, baseIri, fnmlModel, registry);
    }


}
