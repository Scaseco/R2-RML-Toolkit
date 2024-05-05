package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.aksw.rml2.vocab.jena.RML2;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class TestSuiteProcessorRmlKgcw2024 {

    // TODO Consolidate
    public static List<ITriplesMapRml> listAllTriplesMapsRml2(Model model) {
        List<ITriplesMapRml> result = model
                .listSubjectsWithProperty(RML2.logicalSource)
                .mapWith(r -> r.as(TriplesMapRml2.class))
                .mapWith(r -> (ITriplesMapRml)r)
                .toList();
        return result;
    }


    public static void main(String[] args) throws URISyntaxException, IOException {
        List<String> suiteNames = List.of(
                "rml-core"
                // "rml-fnml",
                // "rml-cc", //collections
                // "rml-io",
                // "rml-star"
            );

        // Path p = toPath(Object.class.getResource("Object.class").toURI());
        Path basePath = toPath(TestSuiteProcessorRmlKgcw2024.class.getResource("/kgcw/2024/track1").toURI());

        for (String suiteName : suiteNames) {
            Path suitePath = basePath.resolve(suiteName);

            List<Path> testCases = Files.list(suitePath).toList();
            for (Path testCase : testCases) {
                String name = testCase.getFileName().toString();

                // Skip non-test case folders
                if (!name.startsWith("RMLTC")) {
                    continue;
                }

                Path data = testCase.resolve("data");
                Path shared = data.resolve("shared");
                Path mappingTtl = shared.resolve("mapping.ttl");

                Model model = ModelFactory.createDefaultModel();
                try (InputStream in = Files.newInputStream(mappingTtl)) {
                    RDFDataMgr.read(model, in, Lang.TURTLE);

                    RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

                    List<ITriplesMapRml> tms = listAllTriplesMapsRml2(model);
                    for (ITriplesMapRml tm : tms) {
//                        System.out.println(tm);
                    }

                    RmlToSparqlRewriteBuilder builder = new RmlToSparqlRewriteBuilder()
                            // .setCache(cache)
                            // .addFnmlFiles(fnmlFiles)
                            .addRmlModel(TriplesMapRml2.class, model)
                            .setDenormalize(false)
                            .setMerge(true)
                            ;

                    List<Entry<Query, String>> labeledQueries = builder.generate();

                    for (Entry<Query, String> e : labeledQueries) {
                        System.out.println(e);
                    }

                    // RmlImporter rmlImporter = RmlImporter.from(model);
                    // rmlImporter.process();
                }
                // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
            }

        }

        // Files.list(p).forEach(x -> System.out.println("x: " +x));
        // System.out.println(p);
    }

//    public static Path toFileSystem(URI uri) {
//        // FileSystem fs = FileSystems.getFileSystem(uri);
//
//    }

    // https://stackoverflow.com/a/36021165/160790
    public static Path toPath(URI uri) throws IOException{
        Path result;
        try {
            Path rawPath = Paths.get(uri);
            result = fixPath(rawPath);
        }
        catch(FileSystemNotFoundException ex) {
            // TODO FileSystem needs to be closed
            FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String,Object>emptyMap());
            result = fs.provider().getPath(uri);
        }
        return result;
    }

    public static Path fixPath(Path path) {
        Path result = path;
        Path parentPath = path.getParent();
        if(parentPath != null && !Files.exists(parentPath)) {
            Path fixedCandidatePath = path.resolve("/modules").resolve(path.getRoot().relativize(path));
            result = Files.exists(fixedCandidatePath) ? fixedCandidatePath : path;
        }
        return result;
    }
}
