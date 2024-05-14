package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDouble;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmlTestCaseLister {
    private static final Logger logger = LoggerFactory.getLogger(RmlTestCaseLister.class);

    public static List<RmlTestCase> list(Path basePath, ResourceMgr resourceMgr) throws Exception {
        if (false) {
            Node node = NodeFactory.createLiteral("123.456", XSDDatatype.XSDdouble);
            System.out.println("Node: " + node);

            Object normalized = XSDDatatype.XSDdouble.cannonicalise(node.getLiteralValue());
            System.out.println("Normalized: " + normalized);

            NodeValue nv = NodeValue.makeNode(node);
            System.out.println("NodeValue: " + nv.toString());

            return null;
        }

        RmlTestCaseFactory factory = new RmlTestCaseFactory(resourceMgr);

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
                if (!name.startsWith("RMLTC")) {
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
        Collections.sort(result, new ComparatorChain<>(List.of(
                Comparator.comparingInt(a -> suiteNames.indexOf(a.getSuiteName())),
                (a, b) -> Objects.compare(a.getName(), b.getName(), String::compareTo)
        )
        ));
        return result;
    }

    // https://stackoverflow.com/a/36021165/160790
    public static Path toPath(ResourceMgr resourceMgr, URI uri) throws IOException{
        Path result;
        try {
            Path rawPath = Paths.get(uri);
            result = fixPath(rawPath);
        }
        catch(FileSystemNotFoundException ex) {
            // TODO FileSystem needs to be closed
            FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String,Object>emptyMap());
            resourceMgr.register(fs);
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
