package org.aksw.rml.jena.impl;

import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.rdf.model.Model;

/** Extension of the {@link TriplesMapProcessorRml} to support aliases */
public class TriplesMapProcessorRmlX
    extends TriplesMapProcessorRml
{
    public TriplesMapProcessorRmlX(ITriplesMapRml triplesMap, String baseIri, Model fnmlModel,
            ReferenceFormulationRegistry registry) {
        super(triplesMap, baseIri, fnmlModel, registry);
    }


}
