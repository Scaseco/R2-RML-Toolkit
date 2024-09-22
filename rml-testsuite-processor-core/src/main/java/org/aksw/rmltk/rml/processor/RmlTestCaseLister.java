package org.aksw.rmltk.rml.processor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.rml.jena.plugin.ReferenceFormulationService;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmlTestCaseLister {
    private static final Logger logger = LoggerFactory.getLogger(RmlTestCaseLister.class);

    public static List<RmlTestCase> list(Path basePath, ResourceMgr resourceMgr, ReferenceFormulationService rfRegistry) throws Exception {
        if (false) {
            Node node = NodeFactory.createLiteral("123.456", XSDDatatype.XSDdouble);
            System.out.println("Node: " + node);

            Object normalized = XSDDatatype.XSDdouble.cannonicalise(node.getLiteralValue());
            System.out.println("Normalized: " + normalized);

            NodeValue nv = NodeValue.makeNode(node);
            System.out.println("NodeValue: " + nv.toString());

            return null;
        }

        RmlTestCaseFactory factory = new RmlTestCaseFactory(resourceMgr, rfRegistry);

        List<String> suiteNames = List.of(
            "rml-core"
            // "rml-fnml",
            // "rml-cc", //collections
            // "rml-io",
            // "rml-star"
        );

        // RmlImporterLib.listAllTriplesMaps(null, null)
        // Path p = toPath(Object.class.getResource("Object.class").toURI());

        List<RmlTestCase> result = new ArrayList<>();
        for (String suiteName : suiteNames) {
            Path suitePath = basePath.resolve(suiteName);

            List<Path> testCases = Files.list(suitePath).toList();
            for (Path testCasePath : testCases) {
                String name = testCasePath.getFileName().toString();

                // Skip non-test case folders
                if (!name.startsWith("RML")) {
                    continue;
                }
                // For testing... only use mysql
//                if (!name.endsWith("MySQL")) {
//                    continue;
//                }

                RmlTestCase testCase = factory.loadTestCase(suiteName, testCasePath);
//                if (!name.endsWith("PostgreSQL")) {
//                    continue;
//                }

                if (testCase != null) {
                    result.add(testCase);
                }
//                try {
//                    runTestCase(testCasePath, container, d2rqResolver);
//                } catch (Exception e) {
//                    logger.warn("Failure", e);
//                }
            }
        }
        Collections.sort(result, new ComparatorChain<RmlTestCase>(List.of(
                Comparator.comparingInt(a -> suiteNames.indexOf(a.getSuiteName())),
                //(a, b) -> suiteNames.indexOf(a.getSuiteName()) - suiteNames.indexOf(b.getSuiteName()),
                (a, b) -> Objects.compare(a.getName(), b.getName(), String::compareTo)
            )));

        return result;
    }
}
