package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.jenax.arq.util.quad.DatasetGraphUtils;
import org.aksw.rml.jena.impl.RmlExec;
import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder;
import org.aksw.rml.jena.impl.RmlWorkloadOptimizer;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rmltk.gtfs.GtfsMadridBenchResources;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;
import org.junit.Assert;
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
    public void run() throws IOException {
        // ResourceMgr closes the FileSystem for the class path resource
        try (ResourceMgr resourceMgr = new ResourceMgr()) {
            Path basePath = ResourceMgr.toPath(resourceMgr, GtfsMadridBenchResources.class, name);
            Path mappingFile = basePath.resolve("mapping.csv.rml.ttl");

            RmlToSparqlRewriteBuilder builder = new RmlToSparqlRewriteBuilder()
                .addRmlFile(null, mappingFile)
                .setDenormalize(false)
                .setDistinct(true)
                ;

            List<Entry<Query, String>> labeledQueries = builder.generate();
            Assert.assertEquals(86, labeledQueries.size());

            List<Query> queries = RmlWorkloadOptimizer.newInstance()
                .addSparql(labeledQueries.stream().map(Entry::getKey).toList())
                .process();

            RmlExec rmlExec = RmlExec.newBuilder().addQueries(queries).setRmlMappingDirectory(basePath).build();
            DatasetGraph datasetGraph = rmlExec.toDatasetGraph();
            long tupleCount = DatasetGraphUtils.tupleCount(datasetGraph);
            Assert.assertEquals(395953, tupleCount);
        }
    }
}
