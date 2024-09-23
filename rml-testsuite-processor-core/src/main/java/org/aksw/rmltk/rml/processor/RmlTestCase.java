package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.rml.jena.impl.RmlExec;
import org.aksw.rml.jena.impl.RmlModelImporter;
import org.aksw.rml.jena.impl.RmlModelImporter.RmlInput;
import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder;
import org.aksw.rml.jena.plugin.ReferenceFormulationService;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.apache.curator.shaded.com.google.common.io.MoreFiles;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.AsyncParser;
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
            RmlInput input = RmlModelImporter.processInputCore(TriplesMapRml2.class, rmlMapping.toAbsolutePath().toString(), () -> AsyncParser.of(in, lang, null).streamElements());
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
        RmlExec rmlExec = RmlExec.newBuilder()
                .setLabeledQueries(labeledQueries)
                .setRmlMappingDirectory(rmlMappingDirectory)
                .setD2rqResolver(d2rqResolver)
                .build();
        Dataset result = rmlExec.toDataset();
        return result;
    }

    public String getSuiteName() {
        return suiteName;
    }
}
