package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.model.TriplesMapRml1;
import org.aksw.rmltk.gtfs.GtfsMadridBench;
import org.apache.jena.query.Query;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestRunnerRmlGtfsMadridBench {
    private String name;

    @Parameters(name = "RML TestCase {index}: {0}")
    public static Collection<Object[]> data()
            throws Exception
    {
        JenaSystem.init();
//        InitRmlService.registerServiceRmlSource(ServiceExecutorRegistry.get());

        // Path basePath = RmlTestCaseLister.toPath(resourceMgr, TestRunnerRmlKgcw2024.class.getResource("/kgcw/2024/track1").toURI());

        ReferenceFormulationRegistry rfRegistry = new ReferenceFormulationRegistry();
//        ReferenceFormulationRegistry.registryDefaults(rfRegistry);
//
//
//        // Override registration for JSON to *not* use natural mappings
//        rfRegistry.put(RmlIoTerms.JSONPath, new ReferenceFormulationJsonStrViaService());
//
//
//        List<RmlTestCase> testCases = RmlTestCaseLister.list(basePath, resourceMgr, rfRegistry);
//
//        List<Object[]> params = testCases.stream()
//                .map(testCase -> new Object[] {testCase.getSuiteName() + "::" + testCase.getName(), testCase})
//                .toList();

        List<Object[]> params = List.<Object[]>of(
            new Object[] {"/gtfs-madrid-bench/csv/1" }
        );

        return params;
    }

    protected RmlTestCase testCase;

    public TestRunnerRmlGtfsMadridBench(String name) {
        // this.testCase = testCase;
        this.name = name;
    }

    @Test
    public void run() throws IOException, URISyntaxException {
        ResourceMgr resourceMgr = new ResourceMgr();
        Path basePath = ResourceMgr.toPath(resourceMgr, GtfsMadridBench.class, name);

//        try (Stream<Path> stream = Files.list(basePath)) {
//            System.out.println(stream.toList());
//        }

        Path mappingFile = basePath.resolve("mapping.csv.rml.ttl");
        // System.out.println(Files.lines(mappingFile).toList());
//        resourceMgr.register(RmlTestCaseLister.toPath(resourceMgr, null));
//
        RmlToSparqlRewriteBuilder builder = new RmlToSparqlRewriteBuilder()
            //.setRegistry(referenceFormulationRegistry)
            // .setCache(cache)
            // .addFnmlFiles(fnmlFiles)
            .addRmlFile(TriplesMapRml1.class, mappingFile)
            // .addRmlFile(null, null)
            // .addRmlModel(TriplesMapRml2.class, rmlMapping)
            .setDenormalize(false)
            .setDistinct(true)
            // .setMerge(true)
            ;


//
        List<Entry<Query, String>> labeledQueries = builder.generate();
        System.out.println(labeledQueries);
//
//
//        RmlTestCase.execute(labeledQueries, mappingDirectory, null);
    }
}
