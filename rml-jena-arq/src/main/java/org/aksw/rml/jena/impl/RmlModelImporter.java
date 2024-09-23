package org.aksw.rml.jena.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.EltStreamRDF;
import org.apache.jena.sparql.graph.GraphFactory;

public class RmlModelImporter {

    public static record RmlInput(String file, Set<Class<? extends ITriplesMapRml>> rmlTriplesMapClasses, Model model, String baseIri) {}

    // XXX Allow setting a custom registry; right now we rely on the default one
    // protected Set<Class<? extends ITriplesMap>> triplesMapProbeClasses = new LinkedHashSet<>();

    protected boolean failOnEmptyRml = true;
    protected boolean isValidationRml2Enabled = false;
    protected List<RmlInput> modelAndBaseIriList = new ArrayList<>();

    public RmlModelImporter() {
        // RmlImporterLib.getRegistry().keySet().forEach(this::addTriplesMapProbeClass);
    }

    public List<RmlInput> getInputs() {
        return modelAndBaseIriList;
    }


    public static RmlModelImporter newInstance() {
        return new RmlModelImporter();
    }

//    public RmlImporter addTriplesMapProbeClass(Class<? extends ITriplesMap> probeClass) {
//        Objects.requireNonNull(probeClass);
//        triplesMapProbeClasses.add(probeClass);
//        return this;
//    }
//
//    public RmlImporter setTriplesMapProbeClasses(Collection<Class<? extends ITriplesMap>> probeClasses) {
//        Objects.requireNonNull(probeClasses);
//        triplesMapProbeClasses.clear();
//        triplesMapProbeClasses.addAll(probeClasses);
//        return this;
//    }

    /**
     * Enable validation for all subsequent RML files that are added to this builder.
     * Already added RML files are not affected by this.
     *
     * @param isValidationRml2Enabled
     * @return
     */
    public RmlModelImporter setValidationRml2Enabled(boolean isValidationRml2Enabled) {
        this.isValidationRml2Enabled = isValidationRml2Enabled;
        return this;
    }

    public boolean isValidationRml2Enabled() {
        return isValidationRml2Enabled;
    }

    /** Process a turtle string. */
    public RmlModelImporter addRmlString(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String str) {
        addRmlString(rmlTriplesMapClass, str, Lang.TURTLE);
        return this;
    }

    public RmlModelImporter addRmlString(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String str, Lang lang) {
        RmlInput input = processInput(rmlTriplesMapClass, "inline string",
                () -> AsyncParser.of(new ByteArrayInputStream(str.getBytes()), lang, null).streamElements());
        modelAndBaseIriList.add(input);
        return this;
    }

    public RmlModelImporter addRmlFiles(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Collection<String> rmlFiles) {
        for (String rmlFile : rmlFiles) {
            addRmlFile(rmlTriplesMapClass, rmlFile);
        }
        return this;
    }

    public RmlModelImporter addRmlPaths(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Collection<Path> rmlFiles) {
        for (Path rmlFile : rmlFiles) {
            addRmlFile(rmlTriplesMapClass, rmlFile);
        }
        return this;
    }

    public RmlModelImporter addRmlFile(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String rmlFile) {
        // Model model = RDFDataMgr.loadModel(rmlFile);
        RmlInput input = processInput(rmlTriplesMapClass, rmlFile, () -> AsyncParser.of(rmlFile).streamElements());
        modelAndBaseIriList.add(input);
        return this;
    }

    public RmlModelImporter addRmlFile(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Path rmlFile) {
        // Model model = RDFDataMgr.loadModel(rmlFile);
        try (InputStream in = Files.newInputStream(rmlFile)) {
            Lang lang = RDFDataMgr.determineLang(rmlFile.toString(), null, null);
            RmlInput input = processInput(rmlTriplesMapClass, rmlFile.toAbsolutePath().toString(), () -> AsyncParser.of(in, lang, null).streamElements());
            modelAndBaseIriList.add(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public RmlModelImporter addRmlModel(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Model contrib) {
        modelAndBaseIriList.add(new RmlInput(null, rmlTriplesMapClass == null ? null : Set.of(rmlTriplesMapClass), contrib, null));
        return this;
    }

    public RmlInput processInput(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String inputLabel, Supplier<Stream<EltStreamRDF>> streamSupplier) {
        RmlInput result = processInputCore(rmlTriplesMapClass, inputLabel, streamSupplier);

        if (failOnEmptyRml && result.rmlTriplesMapClasses().isEmpty()) {
            throw new RuntimeException("No rml models detected in " + inputLabel);
        }

        if (isValidationRml2Enabled) {
            Model model = result.model();
            RmlImporterLib.validateRml2(model);
        }
        return result;
    }

    public static RmlInput processInputCore(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String inputLabel, Supplier<Stream<EltStreamRDF>> streamSupplier) {

        // Extract the base IRI needed to succeed on test cases such as RMLTC0020a-CSV and RMLTC0020b-CSV
        String base = null;
        Graph graph = GraphFactory.createDefaultGraph();
        try (Stream<EltStreamRDF> stream = streamSupplier.get()) {
            Iterator<EltStreamRDF> it = stream.iterator();
            while (it.hasNext()) {
                EltStreamRDF elt = it.next();
                if (elt.isBase()) {
                    base = elt.iri();
                } else if (elt.isTriple()) {
                    graph.add(elt.triple());
                } else if (elt.isException()) {
                    throw new RuntimeException("Failed to process input " + inputLabel, elt.exception());
                }
            }
        }

        Model model = ModelFactory.createModelForGraph(graph);

        Set<Class<? extends ITriplesMapRml>> rmlClasses = rmlTriplesMapClass != null
                ? Set.of(rmlTriplesMapClass)
                : RmlImporterLib.detectRml(model);

        return new RmlInput(inputLabel, rmlClasses, model, base);
    }
}
