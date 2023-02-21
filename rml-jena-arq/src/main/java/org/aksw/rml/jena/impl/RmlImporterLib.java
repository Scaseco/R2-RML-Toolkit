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

    public static TriplesMapToSparqlMapping read(TriplesMap tm, Model fnmlModel) {
        TriplesMapToSparqlMapping result = new TriplesMapProcessorRml(tm, fnmlModel).call();
        return result;
    }

    public static Collection<TriplesMapToSparqlMapping> read(Model rawModel, Model fnmlModel) {
        Model model = ModelFactory.createDefaultModel();
        model.add(rawModel);

        List<TriplesMap> triplesMaps = model.listSubjectsWithProperty(Rml.logicalSource).mapWith(r -> r.as(TriplesMap.class)).toList();

//		for(TriplesMap tm : triplesMaps) {
            // TODO Integrate validation with shacl, as this gives us free reports of violations
//		}

        List<TriplesMapToSparqlMapping> result = triplesMaps.stream()
                .map(tm -> read(tm, fnmlModel))
                .collect(Collectors.toList());

        return result;
    }

}
