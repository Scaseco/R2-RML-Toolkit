package org.aksw.rml.jena.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.node.RDFNodeMatcher;
import org.aksw.jenax.arq.util.node.RDFNodeMatchers;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.model.Rml;
import org.aksw.rml.model.TriplesMapRml1;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.aksw.rml2.vocab.jena.RML2;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class RmlImporterLib {

    private static final Map<Class<? extends ITriplesMapRml>, RDFNodeMatcher<? extends ITriplesMapRml>> classToMatcher = new HashMap<>();

    public static void register(Class<? extends ITriplesMapRml> triplesMapClass, RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher) {
        classToMatcher.put(triplesMapClass, nodeMatcher);
    }

    public static RDFNodeMatcher<? extends ITriplesMapRml> getOrThrow(Class<?> triplesMapClass) {
        RDFNodeMatcher<? extends ITriplesMapRml> result = classToMatcher.get(triplesMapClass);
        Objects.requireNonNull(result, "No node matcher found for class: " + triplesMapClass);
        return result;
    }

    static {
        register(TriplesMapRml1.class, RDFNodeMatchers.matchSubjectsWithProperty(TriplesMapRml1.class, Rml.logicalSource));
        register(TriplesMapRml2.class, RDFNodeMatchers.matchSubjectsWithProperty(TriplesMapRml2.class, RML2.logicalSource));
    }

    /**
     * If the given collection of ids is null or empty then read all triples maps from the model.
     * Otherwise only process those with the given ids.
     * Mainly useful for CLI tooling.
     */
    public static Collection<TriplesMapToSparqlMapping> readSpecificOrAll(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Model model, Model fnmlModel, Collection<String> triplesMapIds, ReferenceFormulationRegistry registry) {
        RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher = getOrThrow(rmlTriplesMapClass);
        return readSpecificOrAll(nodeMatcher, rmlTriplesMapClass, model, fnmlModel, triplesMapIds, registry);
    }

    public static Collection<TriplesMapToSparqlMapping> readSpecificOrAll(RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher, Class<? extends ITriplesMapRml> rmlTriplesMapClass, Model model, Model fnmlModel, Collection<String> triplesMapIds, ReferenceFormulationRegistry registry) {
        Collection<TriplesMapToSparqlMapping> result;
        if (triplesMapIds != null && !triplesMapIds.isEmpty()) {
            result = triplesMapIds.stream()
                .map(id -> model.createResource(id).as(rmlTriplesMapClass))
                .map(tm -> RmlImporterLib.read(tm, fnmlModel, registry))
                .collect(Collectors.toList());
        } else {
            result = RmlImporterLib.read(nodeMatcher, model, fnmlModel);
        }
        return result;
    }

    public static TriplesMapToSparqlMapping read(ITriplesMapRml tm, Model fnmlModel, ReferenceFormulationRegistry registry) {
        TriplesMapToSparqlMapping result = new TriplesMapProcessorRml(tm, null, fnmlModel, registry).call();
        return result;
    }

    public static TriplesMapToSparqlMapping read(ITriplesMapRml tm, Model fnmlModel) {
        return read(tm, fnmlModel, null);
    }

//    public static TriplesMapToSparqlMapping readFno(ITriplesMap tm, Model fnmlModel, ReferenceFormulationRegistry registry) {
//        TriplesMapToSparqlMapping result = new TriplesMapProcessorR2rml(tm, null).call(); //, fnmlModel, registry).call();
//        return result;
//    }
//
//    public static TriplesMapToSparqlMapping readFno(ITriplesMap tm, Model fnmlModel) {
//        return readFno(tm, fnmlModel, null);
//    }

    /** List all resources that have a rml:logicalSource property */
    public static List<ITriplesMapRml> listAllTriplesMaps(Class<? extends ITriplesMapRml> triplesMapClass, Model model) {
        RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher = getOrThrow(triplesMapClass);
        return listAllTriplesMaps(nodeMatcher, model);
    }

    public static List<ITriplesMapRml> listAllTriplesMaps(RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher, Model model) {


        List<ITriplesMapRml> result = nodeMatcher.match(model)
                // .listSubjectsWithProperty(Rml.logicalSource)
                // .mapWith(r -> r.as(TriplesMapRml1.class))
                .mapWith(r -> (ITriplesMapRml)r)
                .toList();
        return result;
    }

    public static Collection<TriplesMapToSparqlMapping> read(Class<? extends ITriplesMapRml> triplesMapClass, Model rawModel, Model fnmlModel) {
        RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher = getOrThrow(triplesMapClass);
        return read(nodeMatcher, rawModel, fnmlModel);
    }

    public static Collection<TriplesMapToSparqlMapping> read(RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher, Model rawModel, Model fnmlModel) {

        Model model = ModelFactory.createDefaultModel();
        model.add(rawModel);

        List<ITriplesMapRml> triplesMaps = listAllTriplesMaps(nodeMatcher, model);

//		for(TriplesMap tm : triplesMaps) {
            // TODO Integrate validation with shacl, as this gives us free reports of violations
//		}

        List<TriplesMapToSparqlMapping> result = triplesMaps.stream()
                .map(tm -> read(tm, fnmlModel))
                .collect(Collectors.toList());

        return result;
    }
}
