package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.r2rml.jena.arq.impl.R2rmlImporterLib;
import org.aksw.r2rml.jena.jdbc.processor.R2rmlProcessorJdbc;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLib;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLoader;
import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.vocabulary.XSD;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class R2rmlTestSuiteProcessorH2 {
    private static final Logger logger = LoggerFactory.getLogger(R2rmlTestSuiteProcessorH2.class);

    public static void main(String[] args) throws SQLException, IOException {

        SqlCodec sqlCodec = SqlCodecUtils.createSqlCodecDefault();

        if (false) {
            RDFDatatype dtype = TypeMapper.getInstance().getTypeByName(XSD.xdouble.getURI());
            Node a = NodeFactory.createLiteral("80.25", dtype);
            Node b = NodeFactory.createLiteral("8.025E1", dtype);

            NodeValue na = NodeValue.makeNode(a);
            NodeValue nb = NodeValue.makeNode(b);
            System.out.println("Value equality: " + NodeValue.sameValueAs(na, nb));
            System.out.println(na.getDouble() == nb.getDouble());
            System.out.println("Term equality: " + a.equals(b));
            return;
        }

        // Collection<Database> databases = R2rmlTestCaseLoader.importDatabases();
        Dataset manifests = R2rmlTestCaseLoader.loadManifests(true);

        List<String> manifestNames = Streams.stream(manifests.listNames()).collect(Collectors.toList());

        for (String manifestName : manifestNames) {

            logger.info("Processing manifest: " + manifestName);
            Model model = manifests.getNamedModel(manifestName);

            Collection<R2rmlTestCase> testCases = R2rmlTestCaseLib.readTestCases(model);
            for (R2rmlTestCase testCase : testCases) {
                String testCaseId = testCase.getIdentifier();

                boolean isOnSkipList = Arrays.asList(
                        // Skipped due non-deterministic failures due to rounding errors in double-typed literals:
                        // Isomorphism by value still not always matches terms represented as 3.0E1 vs "20.0"^^xsd:double
                        "R2RMLTC0012a",
                        "R2RMLTC0012e",
                        "R2RMLTC0005b"

                        // Below skip list is deprecated
                        // "R2RMLTC0016b", // canonical double representation issue
                        // "R2RMLTC0020a", // Skipped because of encode-for-url application on value basis, mix of absolute and relative IRIs in column
                        // "R2RMLTC0019a", // Mixed absolute and relative IRIs
                        // "R2RMLTC0012e" // fails/succeeds indeterministically; appears to be double rounding issues
                        // "R2RMLTC0003a", // Tests SQL version identifiers; this should be captured by test whether all terms in the r2rml namespace are known
                        // "R2RMLTC0015b"  // Either Jena's LangTag.check() is too permissive or the test case too strict (lang tags "spanish" and "english" used)
                        ).contains(testCaseId);
                if (isOnSkipList) {
                    System.err.println("Skipping " + isOnSkipList + " due to skip list");
                    continue;
                }


//				if (!testCaseId.equals("R2RMLTC0015b")) {
//					continue;
//				}


//				Model closureModel = ResourceUtils.reachableClosure(testCase);
//				RDFDataMgr.write(System.out, closureModel, RDFFormat.TURTLE_PRETTY);


                logger.info("Processing test case: " + testCaseId);


                Database database = testCase.getDatabase();

                if (database != null) {
                    DataSource dataSource = null;
                    try {
                        dataSource = H2Utils.prepareDataSource(database);

                        try (Connection conn = dataSource.getConnection()) {

                            Dataset expectedOutput = R2rmlTestCaseLib.loadOutput(testCase);

                            if (testCase.getHasExpectedOutput()) {
                                if (expectedOutput == null) {
                                    throw new IllegalStateException("Test case declared with expected output - but no such output defined");
                                }
                            } else {
                                if (expectedOutput != null) {
                                    logger.warn("Test case declared with no expected output - but output defined; preferring defined output");
                                } else {
                                    // Create an empty model to compare to if we don't expect any output
                                    expectedOutput = DatasetFactory.create();
                                }
                            }

                            Model r2rmlDocument = R2rmlTestCaseLib.loadMappingDocument(testCase);

                            R2rmlImporterLib.validateR2rml(r2rmlDocument);

                            String baseIri = "http://example.com/base/";
                            Dataset actualOutput = R2rmlProcessorJdbc.processR2rml(conn, r2rmlDocument, baseIri, sqlCodec);

                            boolean isIso = isIsomorphic(expectedOutput, actualOutput, true);
                            logger.debug("Expected result equals expected one by value -> " + isIso);
                            System.out.println("Asserted " + testCase.getIdentifier() + " " + (isIso ? "[ OK ]" : "[FAIL]"));
                            Assert.assertTrue(isIso);
                        }

                    } catch (Exception e) {
                        String failMessage = testCase.getFailMessage();
                        if (failMessage != null) {
                            System.out.println("Test failed as expected: " + failMessage);
                        } else {
                            throw new RuntimeException("Test failed unexpectedly", e);
                        }

                    } finally {
                        if (dataSource != null) {
                            H2Utils.shutdownH2(dataSource);
                        }
                    }


                }
            }
        }
    }




    /**
     * Check two datasets for isomorphism using comparison by value.
     * Internally converts the datasets into result sets with ?g ?s ?p ?o) bindings
     * and compares them using {@link ResultSetCompare#equalsByValue(org.apache.jena.query.ResultSet, org.apache.jena.query.ResultSet)}
     *
     *
     * @param expected
     * @param actual
     * @param compareByValue 'false' tests for equivalence of terms whereas 'true' tests for that of values
     * @return
     */
    public static boolean isIsomorphic(Dataset expected, Dataset actual, boolean compareByValue) {
        boolean result;

        String everything = "SELECT ?g ?s ?p ?o { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } }";
//		String everything = "SELECT ?o { { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } } FILTER(isNumeric(?o))}";
        try (QueryExecution qea = QueryExecutionFactory.create(everything, expected);
            QueryExecution qeb = QueryExecutionFactory.create(everything, actual)) {

            ResultSetRewindable rsa = ResultSetFactory.copyResults(qea.execSelect());
            ResultSetRewindable rsb = ResultSetFactory.copyResults(qeb.execSelect());

            result = compareByValue
                    ? ResultSetCompare.equalsByValue(rsa, rsb)
                    : ResultSetCompare.equalsByTerm(rsa, rsb);

            if (!result) {
                rsa.reset();
                rsb.reset();
                System.out.println("Expected:");
                ResultSetFormatter.out(rsa);
                System.out.println("Actual:");
                ResultSetFormatter.out(rsb);
            }
        }

        return result;
    }

//	/**
//	 * Difference of two collections w.r.t. a custom comparator.
//	 * Runs in O(n^2)
//	 *
//	 * @param <T>
//	 * @param a
//	 * @param b
//	 * @param cmp
//	 * @return
//	 */
//	public static <T> Set<T> difference(
//			Collection<? extends T> a ,
//			Collection<? extends T> b,
//			Comparator<? super T> cmp) {
//		Set<T> result = a.stream()
//			.filter(ai -> b.stream().noneMatch(bi -> cmp.compare(ai, bi) == 0))
//			.collect(Collectors.toSet());
//
//
////		System.out.println("Diff:");
////		result.forEach(System.out::println);
//		return result;
//	}
//
//
//    public static int compareAlways(NodeValue nv1, NodeValue nv2)
//    {
//    	int result;
//        try {
//            result = NodeValue.compare(nv1, nv2);
//        } catch (ExprNotComparableException ex) {
//        	result =  NodeUtils.compareRDFTerms(nv1.asNode(), nv2.asNode());
//        }
//        return result;
//    }
//
//	/**
//	 * Compare nodes such that e.g. "10"^^xsd:int is equal to "10"^^xsd:integer
//	 *
//	 * @param a
//	 * @param b
//	 * @return
//	 */
//	public static int compareNodesViaNodeValue(Node a, Node b) {
//		NodeValue nva = NodeValue.makeNode(a);
//		NodeValue nvb = NodeValue.makeNode(b);
//		int result = compareAlways(nva, nvb);
//		System.out.println(nva + (result == 0 ? " == " : " =/=") + nvb);
//		return result;
//	}
//
//	public static int compareNodesEquiv(Node a, Node b) {
//		int result = Quad.isDefaultGraph(a) && Quad.isDefaultGraph(b)
//				? 0
//				: compareNodesViaNodeValue(a, b);
//		return result;
//	}
//
//
//	public static int compareQuadsViaNodeValue(Quad a, Quad b) {
//		return compareQuads(a, b, R2rmlTestSuiteProcessorH2::compareNodesEquiv);
//	}
//
//	public static int compareQuads(Quad a, Quad b, Comparator<? super Node> nodeComparator) {
//		int result = ComparisonChain.start()
//			.compare(a.getGraph(), b.getGraph(), nodeComparator)
//			.compare(a.getSubject(), b.getSubject(), nodeComparator)
//			.compare(a.getPredicate(), b.getPredicate(), nodeComparator)
//			.compare(a.getObject(), b.getObject(), nodeComparator)
//			.result();
//
//		return result;
//	}

}
