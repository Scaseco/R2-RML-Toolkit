package org.aksw.rmltk.rml.processor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.rml.jena.impl.RmlImporterLib;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class RmlTestCaseFactory {
    protected Logger logger = LoggerFactory.getLogger(RmlTestCaseFactory.class);

    protected Map<String, JdbcDatabaseContainer<?>> containers = new HashMap<>();
    protected Consumer<D2rqDatabase> d2rqResolver;

    protected ResourceMgr resourceMgr;

    public RmlTestCaseFactory(ResourceMgr resourceMgr) {
        this.resourceMgr = Objects.requireNonNull(resourceMgr);
        init();
    }

    /** start the container if needed and return its IP */
    public static String getIp(GenericContainer<?> container) {
        String result = container.getContainerInfo().getNetworkSettings().getNetworks().values().iterator().next().getIpAddress();
        return result;
    }

    protected void init() {
        MySQLContainer<?> mysqlContainer = resourceMgr.register(new MySQLContainer<>("mysql:5.7.34")
                .withUsername("root")
                .withPassword("root")
                // .withExposedPorts(3306)
                .withDatabaseName("db")
                .withLogConsumer(of -> logger.info(of.getUtf8String())));

        PostgreSQLContainer<?> postgresqlContainer = resourceMgr.register(new PostgreSQLContainer<>("pgvector/pgvector:pg16")
                .withUsername("root")
                .withPassword("root")
                // .withExposedPorts(3306)
                .withDatabaseName("db")
                .withLogConsumer(of -> logger.info(of.getUtf8String())));

        containers.put("MySQL", mysqlContainer);
        containers.put("PostgreSQL", postgresqlContainer);

        d2rqResolver = r -> {
            String before = r.getJdbcDSN();
            String after = null;
            for (Entry<String, JdbcDatabaseContainer<?>> c : containers.entrySet()) {
                // Case matters: jdbc:mysql://MySQL:3306/db - we want to replace the hostname not the scheme
                String name = c.getKey();
                if (before.contains(name)) {
                    String host = getIp(mysqlContainer);
                    after = before.replace(name, host);
                }
            }

            if (after != null) {
                r.setJdbcDSN(after);
            }
            System.err.println("Connection Spec:" + r);
        };
    }

    public RmlTestCase generateTestCase(Path testCasePath) {
        RmlTestCase result = null;
        String name = testCasePath.getFileName().toString();

        // Get last element of array after splitting by '-'
        String suffix = Arrays.asList(name.split("-")).stream().reduce((a,  b) -> b).get();

        JdbcDatabaseContainer<?> container = containers.get(suffix);

        try {
            result = generateTestCase(testCasePath, container);
        } catch (Exception e) {
            logger.warn("Failure", e);
        }
        return result;
    }

    protected RmlTestCase generateTestCase(Path testCase, JdbcDatabaseContainer<?> container) throws Exception {

        String name = testCase.getFileName().toString();
        System.out.println("Running: " + name);

        Path data = testCase.resolve("data");
        Path shared = data.resolve("shared");
        Path mappingTtl = shared.resolve("mapping.ttl");

        Path resourceSql = shared.resolve("resource.sql");

        Path expectedPath = shared.resolve("expected");
        Path outputNq = expectedPath.resolve("output.nq");

        Dataset expectedDs = DatasetFactory.create();
        try (InputStream in = Files.newInputStream(outputNq)) {
            RDFDataMgr.read(expectedDs, in, Lang.TRIG);
        }

        Model mappingModel = ModelFactory.createDefaultModel();
        try (InputStream in = Files.newInputStream(mappingTtl)) {
            RDFDataMgr.read(mappingModel, in, Lang.TURTLE);
        }

        List<ITriplesMapRml> tms = RmlImporterLib.listAllTriplesMaps(TriplesMapRml2.class, mappingModel);
        for (ITriplesMapRml tm : tms) {
//                System.out.println(tm);
        }

        return new RmlTestCase(name, mappingModel, shared, expectedDs, d2rqResolver, container, resourceSql);
    }
}
