package org.aksw.rml.jena.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.node.RDFNodeMatcher;
import org.aksw.jenax.arq.util.node.RDFNodeMatchers;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.r2rml.jena.arq.impl.R2rmlImporterLib;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.plugin.ReferenceFormulationService;
import org.aksw.rml.model.Rml;
import org.aksw.rml.model.TriplesMapRml1;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.aksw.rml2.vocab.jena.RML2;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

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

    public static void validateRml2Language(Model model) {
        R2rmlImporterLib.validateLangTagsOfProperty(model, RML2.language);
    }

    public static void validateRml2(Model rml2Model) {
        // FIXME Need to add a rml2 ontology file
        Model rml2OntModel = RDFDataMgr.loadModel("rml.v2.core.owl.ttl");

        Set<String> undefinedUses = R2rmlImporterLib.checkForUsageOfUndefinedTerms(rml2OntModel, rml2Model, R2rmlTerms.uri);

        if (!undefinedUses.isEmpty()) {
            throw new RuntimeException("Used terms without definition: " + undefinedUses);
        }

        Model shaclModel = RDFDataMgr.loadModel("rml.v2.core.shacl.ttl");

        // Perform the validation of everything, using the data model
        // also as the shapes model - you may have them separated
//		Resource result = ValidationUtil.validateModel(dataModel, shaclModel, true);

        ValidationReport report;
        try {
            report = ShaclValidator.get().validate(shaclModel.getGraph(), rml2Model.getGraph());
        } catch (Exception e) {
            RDFDataMgr.write(System.err, shaclModel, RDFFormat.NTRIPLES);
            throw new RuntimeException("Internal error during shacl validation - model printed to stderr", e);
        }


//	    ShLib.printReport(report);
//	    System.out.println();
//	    RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);

        boolean conforms = report.conforms();
//		boolean conforms = result.getProperty(SH.conforms).getBoolean();

        if(!conforms) {
            // Print violations
            ShLib.printReport(report);
            RDFDataMgr.write(System.err, report.getModel(), RDFFormat.TURTLE_PRETTY);
            throw new RuntimeException("Shacl validation failed; see report above");
        }

        // Check all values given for rr:languages (demanded by test case R2RMLTC0015b)
        // this could be handled with sparql extension functions + shacl in the future
        validateRml2Language(rml2Model);
    }

    /**
     * If the given collection of ids is null or empty then read all triples maps from the model.
     * Otherwise only process those with the given ids.
     * Mainly useful for CLI tooling.
     */
    public static Collection<TriplesMapToSparqlMapping> readSpecificOrAll(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Model model, Model fnmlModel, Collection<String> triplesMapIds, ReferenceFormulationService registry) {
        RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher = getOrThrow(rmlTriplesMapClass);
        return readSpecificOrAll(nodeMatcher, rmlTriplesMapClass, model, fnmlModel, triplesMapIds, registry);
    }

    public static Collection<TriplesMapToSparqlMapping> readSpecificOrAll(RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher, Class<? extends ITriplesMapRml> rmlTriplesMapClass, Model model, Model fnmlModel, Collection<String> triplesMapIds, ReferenceFormulationService registry) {
        Collection<TriplesMapToSparqlMapping> result;
        if (triplesMapIds != null && !triplesMapIds.isEmpty()) {
            result = triplesMapIds.stream()
                .map(id -> model.createResource(id).as(rmlTriplesMapClass))
                .map(tm -> RmlImporterLib.read(tm, fnmlModel, registry))
                .collect(Collectors.toList());
        } else {
            result = RmlImporterLib.read(nodeMatcher, model, fnmlModel, registry);
        }
        return result;
    }

    public static TriplesMapToSparqlMapping read(ITriplesMapRml tm, Model fnmlModel, ReferenceFormulationService registry) {
        TriplesMapProcessorRml processor = new TriplesMapProcessorRml(tm, null, fnmlModel, registry);
        TriplesMapToSparqlMapping result = processor.call();
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

    public static Collection<TriplesMapToSparqlMapping> read(Class<? extends ITriplesMapRml> triplesMapClass, Model rawModel, Model fnmlModel, ReferenceFormulationService registry) {
        RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher = getOrThrow(triplesMapClass);
        return read(nodeMatcher, rawModel, fnmlModel, registry);
    }

    public static Collection<TriplesMapToSparqlMapping> read(RDFNodeMatcher<? extends ITriplesMapRml> nodeMatcher, Model rawModel, Model fnmlModel, ReferenceFormulationService registry) {

        Model model = ModelFactory.createDefaultModel();
        model.add(rawModel);

        List<ITriplesMapRml> triplesMaps = listAllTriplesMaps(nodeMatcher, model);

//		for(TriplesMap tm : triplesMaps) {
            // TODO Integrate validation with shacl, as this gives us free reports of violations
//		}


//        List<TriplesMapToSparqlMapping> result = triplesMaps.stream()
//                .map(tm -> read(tm, fnmlModel, registry))
//                .collect(Collectors.toList());

        List<TriplesMapToSparqlMapping> result = new ArrayList<>(triplesMaps.size());
        for (ITriplesMapRml triplesMap : triplesMaps) {
            TriplesMapToSparqlMapping contrib = read(triplesMap, fnmlModel, registry);
            result.add(contrib);
        }

        return result;
    }
}
