package org.aksw.rml.jena.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.rml.model.Rml;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class RmlImporterLib {

    /**
     * If the given collection of ids is null or empty then read all triples maps from the model.
     * Otherwise only process those with the given ids.
     * Mainly useful for CLI tooling.
     */
    public static Collection<TriplesMapToSparqlMapping> readSpecificOrAll(Model model, Model fnmlModel, Collection<String> triplesMapIds) {
        Collection<TriplesMapToSparqlMapping> result;
        if (triplesMapIds != null && !triplesMapIds.isEmpty()) {
            result = triplesMapIds.stream()
                .map(id -> model.createResource(id).as(TriplesMap.class))
                .map(tm -> RmlImporterLib.read(tm, fnmlModel))
                .collect(Collectors.toList());
        } else {
            result = RmlImporterLib.read(model, fnmlModel);
        }
        return result;
    }

    public static TriplesMapToSparqlMapping read(TriplesMap tm, Model fnmlModel) {
        TriplesMapToSparqlMapping result = new TriplesMapProcessorRml(tm, fnmlModel).call();
        return result;
    }

    public static Collection<TriplesMapToSparqlMapping> read(Model rawModel, Model fnmlModel) {

        Model model = ModelFactory.createDefaultModel();
        model.add(rawModel);

        List<TriplesMap> triplesMaps = model
                .listSubjectsWithProperty(Rml.logicalSource)
                .mapWith(r -> r.as(TriplesMap.class))
                .toList();

//		for(TriplesMap tm : triplesMaps) {
            // TODO Integrate validation with shacl, as this gives us free reports of violations
//		}

        List<TriplesMapToSparqlMapping> result = triplesMaps.stream()
                .map(tm -> read(tm, fnmlModel))
                .collect(Collectors.toList());

        return result;
    }

}
