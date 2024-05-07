package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.rml.jena.impl.RmlImporterLib;
import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder;
import org.aksw.rml.jena.service.InitRmlService;
import org.aksw.rml.jena.service.RmlSymbols;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.curator.shaded.com.google.common.io.MoreFiles;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionDatasetBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

public class TestSuiteProcessorRmlKgcw2024 {

    static { JenaSystem.init(); }

    private static final Logger logger = LoggerFactory.getLogger(TestSuiteProcessorRmlKgcw2024.class);

    // TODO Consolidate
//    public static List<ITriplesMapRml> listAllTriplesMapsRml2(Model model) {
//        List<ITriplesMapRml> result = model
//                .listSubjectsWithProperty(RML2.logicalSource)
//                .mapWith(r -> r.as(TriplesMapRml2.class))
//                .mapWith(r -> (ITriplesMapRml)r)
//                .toList();
//        return result;
//    }
//

    public static void main(String[] args) throws Exception {
        InitRmlService.registerServiceRmlSource(ServiceExecutorRegistry.get());

        try (MySQLContainer<?> dbContainer = new MySQLContainer<>("mysql:5.7.34")
                .withUsername("root")
                .withPassword("root")
                // .withExposedPorts(3306)
                .withDatabaseName("db")
                .withLogConsumer(of -> logger.info(of.getUtf8String()))) {
            dbContainer.start();
            run(dbContainer);
            dbContainer.stop();
        }
    }


//    public static void main(String[] args) throws URISyntaxException, IOException {
//        InitRmlService.registerServiceRmlSource(ServiceExecutorRegistry.get());
//
//        try (PostgreSQLContainer<?> dbContainer = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
//                 .withLogConsumer(of -> logger.info(of.getUtf8String()))) {
//            dbContainer.start();
//            run();
//            dbContainer.stop();
//        }
//    }


    public static void run(JdbcDatabaseContainer<?> jdbcContainer) throws Exception {
        //dbContainer.getJdbcUrl()

        String host = jdbcContainer.getContainerInfo().getNetworkSettings().getNetworks().values().iterator().next().getIpAddress();

        Consumer<D2rqDatabase> d2rqResolver = r -> {
            String before = r.getJdbcDSN();
            String after = before.replace("MySQL", host);
            r.setJdbcDSN(after);
            System.err.println("Connection Spec:" + r);
        };

        List<String> suiteNames = List.of(
            "rml-core"
            // "rml-fnml",
            // "rml-cc", //collections
            // "rml-io",
            // "rml-star"
        );

        // RmlImporterLib.listAllTriplesMaps(null, null)
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

                try {
                    runTestCase(testCase, jdbcContainer, d2rqResolver);
                } catch (Exception e) {
                    logger.warn("Failure", e);
                }
            }

        }

        // Files.list(p).forEach(x -> System.out.println("x: " +x));
        // System.out.println(p);
    }


    public static void runTestCase(Path testCase, JdbcDatabaseContainer<?> jdbcContainer, Consumer<D2rqDatabase> d2rqResolver) throws Exception {

        String name = testCase.getFileName().toString();

        Path data = testCase.resolve("data");
        Path shared = data.resolve("shared");
        Path mappingTtl = shared.resolve("mapping.ttl");

        Path resourceSql = shared.resolve("resource.sql");
        if (Files.exists(resourceSql)) {
            String str = MoreFiles.asCharSource(resourceSql, StandardCharsets.UTF_8).read();
            String[] strs = str.split(";");

            try (Connection conn = jdbcContainer.createConnection("")) {
                for (String s : strs) {
                    s = s.replaceAll("^[\n\r ]+", "").replaceAll("[\n\r ]+$", "");
                    if (!s.isEmpty()) {
                        System.out.println("Executing:");
                        System.out.println(s);
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(s);
                        }
                    }
                }
            }
        }

        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = Files.newInputStream(mappingTtl)) {
            RDFDataMgr.read(model, in, Lang.TURTLE);

            System.out.println("PROCESSING: " + name);
            System.out.println("-------------------------------------------------------");
            RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

            try {
                List<ITriplesMapRml> tms = RmlImporterLib.listAllTriplesMaps(TriplesMapRml2.class, model);
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
                System.out.println("Generated " + labeledQueries.size() + " queries");

                Model emptyModel = ModelFactory.createDefaultModel();
                for (Entry<Query, String> e : labeledQueries) {
                    Query query = e.getKey();

                    StreamRDF sink = StreamRDFWriter.getWriterStream(System.out, RDFFormat.TRIG_BLOCKS);
                    sink.start();

                    try (QueryExecution qe = QueryExecutionDatasetBuilder.create()
                            .model(emptyModel)
                            .query(query)
                            .set(RmlSymbols.symMappingDirectory, shared)
                            .set(RmlSymbols.symD2rqDatabaseResolver, d2rqResolver)
                            .build()) {
                        Iterator<Quad> it = qe.execConstructQuads();
                        while (it.hasNext()) {
                            sink.quad(it.next());
                        }
                    }
                    sink.finish();

                    // System.out.println(e);
                }

//                SparqlScriptProcessor processor = SparqlScriptProcessor.createPlain(null, null);
//                processor.process



            } catch (Exception e) {
                System.err.println("ERROR");
                e.printStackTrace();
            }
            // RmlImporter rmlImporter = RmlImporter.from(model);
            // rmlImporter.process();
        }
        // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
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
