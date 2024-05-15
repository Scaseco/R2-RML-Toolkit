package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.jenax.arq.util.exec.query.JenaXSymbols;
import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder;
import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder.Input;
import org.aksw.rml.jena.plugin.ReferenceFormulationService;
import org.aksw.rml.jena.service.RmlSymbols;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.apache.curator.shaded.com.google.common.io.MoreFiles;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionDatasetBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

public class RmlTestCase
    implements Callable<Dataset>
{
    private static final Logger logger = LoggerFactory.getLogger(RmlTestCase.class);

    protected String name;
    protected String suiteName;

    // protected Model rmlMapping;
    protected Path rmlMapping;
    protected Path rmlMappingDirectory;

    protected Map<String, Dataset> expectedResult;
    protected boolean expectedFailure;

    protected ReferenceFormulationService referenceFormulationRegistry;
    protected Consumer<D2rqDatabase> d2rqResolver;

    protected JdbcDatabaseContainer<?> jdbcContainer;
    protected Path resourceSql;

    public RmlTestCase(String name, Path rmlMapping, Path rmlMappingDirectory, Map<String, Dataset> expectedResultDses, boolean expectedFailure,
            ReferenceFormulationService referenceFormulationRegistry, Consumer<D2rqDatabase> d2rqResolver, JdbcDatabaseContainer<?> jdbcContainer, Path resourceSql) {
        super();
        this.name = name;
        this.rmlMapping = rmlMapping;
        this.rmlMappingDirectory = rmlMappingDirectory;
        this.expectedResult = expectedResultDses;
        this.expectedFailure = expectedFailure;
        this.referenceFormulationRegistry = referenceFormulationRegistry;
        this.d2rqResolver = d2rqResolver;
        this.jdbcContainer = jdbcContainer;
        this.resourceSql = resourceSql;
    }

    public Map<String, Dataset> getExpectedResultDses() {
        return expectedResult;
    }

    public String getName() {
        return name;
    }

    public boolean isExpectedFailure() {
        return expectedFailure;
    }

    public Model loadModel() {
        Model result;
        try (InputStream in = Files.newInputStream(rmlMapping)) {
            Lang lang = RDFDataMgr.determineLang(rmlMapping.toString(), null, null);
            Input input = RmlToSparqlRewriteBuilder.processInput(TriplesMapRml2.class, rmlMapping.toAbsolutePath().toString(), () -> AsyncParser.of(in, lang, null).streamElements());
            result = input.model();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Dataset call() throws Exception {
        List<Entry<Query, String>> labeledQueries = generateQueries();

        if (jdbcContainer != null && !jdbcContainer.isRunning()) {
            jdbcContainer.start();
        }

        setupDatabase();

        Dataset result = execute(labeledQueries);
        return result;
    }

    protected List<Entry<Query, String>> generateQueries() {
        RmlToSparqlRewriteBuilder builder = new RmlToSparqlRewriteBuilder()
                .setRegistry(referenceFormulationRegistry)
                // .setCache(cache)
                // .addFnmlFiles(fnmlFiles)
                .addRmlFile(TriplesMapRml2.class, rmlMapping)
                // .addRmlModel(TriplesMapRml2.class, rmlMapping)
                .setDenormalize(false)
                .setDistinct(true)
                // .setMerge(true)
                ;

        List<Entry<Query, String>> labeledQueries = builder.generate();

        return labeledQueries;
    }

    public void setupDatabase() throws Exception {
        if (Files.exists(resourceSql)) {
            String str = MoreFiles.asCharSource(resourceSql, StandardCharsets.UTF_8).read();
            // Splitting by ';' is brittle but a common best-effort approach
            String[] strs = str.split(";");

            try (Connection conn = jdbcContainer.createConnection("")) {
                for (String s : strs) {
                    // Remove leading and trailing whitespaces _and_ newlines
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
    }

    public Dataset execute(List<Entry<Query, String>> labeledQueries) {
        Dataset result = execute(labeledQueries, rmlMappingDirectory, d2rqResolver);
        return result;
//    	Dataset result = DatasetFactory.create();
//    	new SinkQuadsToDataset(true, ds);
//    	execute(labele)
    }

    // FIXME Migrate to StringRDF rather than returning a Dataset directly
    public static Dataset execute(List<Entry<Query, String>> labeledQueries, Path rmlMappingDirectory, Consumer<D2rqDatabase> d2rqResolver) {
        Model emptyModel = ModelFactory.createDefaultModel();
        Dataset actualDs = DatasetFactory.create();

        for (Entry<Query, String> e : labeledQueries) {
            Query query = e.getKey();
            logger.info("Executing SPARQL Query: " + query);
            try (ResourceMgr qExecResMgr = new ResourceMgr();
                 QueryExecution qe = QueryExecutionDatasetBuilder.create()
                     .model(emptyModel)
                     .query(query)
                     .set(RmlSymbols.symMappingDirectory, rmlMappingDirectory)
                     .set(RmlSymbols.symD2rqDatabaseResolver, d2rqResolver)
                     .set(JenaXSymbols.symResourceMgr, qExecResMgr)
                     .build()) {

                logger.info("Begin of RDF data Contribution:");
                StreamRDF sink = StreamRDFWriter.getWriterStream(System.err, RDFFormat.TRIG_BLOCKS);
                sink.start();

                Iterator<Quad> it = qe.execConstructQuads();
                while (it.hasNext()) {
                    Quad quad = it.next();
                    sink.quad(quad);
                    actualDs.asDatasetGraph().add(quad);
                }

                sink.finish();
                logger.info("End of RDF data contribution");
            }
        }

        return actualDs;
    }

    public String getSuiteName() {
        return suiteName;
    }
}
