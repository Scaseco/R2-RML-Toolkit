package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.aksw.r2rml.jena.arq.impl.R2rmlImporter;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.jdbc.api.RowMapper;
import org.aksw.r2rml.jena.jdbc.util.JdbcUtils;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLib;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLoader;
import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ext.com.google.common.collect.ComparisonChain;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.XSD;
import org.junit.Assert;


public class R2rmlTestSuiteProcessorH2 {
	public static void main(String[] args) throws SQLException, IOException {

		if (false) {
			RDFDatatype dtype = TypeMapper.getInstance().getTypeByName(XSD.xdouble.getURI());
			Node a = NodeFactory.createLiteral("80.25", dtype);
			Node b = NodeFactory.createLiteral("8.025E1", dtype);
			
			NodeValue na = NodeValue.makeNode(a);
			NodeValue nb = NodeValue.makeNode(b);
			System.out.println(NodeValue.sameAs(na, nb));
			System.out.println(na.getDouble() == nb.getDouble());
			return;
		}
		
		// Collection<Database> databases = R2rmlTestCaseLoader.importDatabases();
		Dataset manifests = R2rmlTestCaseLoader.loadManifests(true);
		
		List<String> manifestNames = Streams.stream(manifests.listNames()).collect(Collectors.toList());
		
		for (String manifestName : manifestNames) {
			
			System.out.println("Processing manifest: " + manifestName);
			Model model = manifests.getNamedModel(manifestName);
			
			Collection<R2rmlTestCase> testCases = R2rmlTestCaseLib.readTestCases(model);
			for (R2rmlTestCase testCase : testCases) {
				String testCaseId = testCase.getIdentifier();
				System.out.println("Processing test case: " + testCaseId);

//				Model closureModel = ResourceUtils.reachableClosure(testCase);
//				RDFDataMgr.write(System.out, closureModel, RDFFormat.TURTLE_PRETTY);
				
				if (!testCase.getHasExpectedOutput()) {
					continue;
				}
				
				Database database = testCase.getDatabase();
							
				if (database != null) {
					DataSource dataSource = null;
					try {
						dataSource = H2Utils.prepareDataSource(database);
				
						try (Connection conn = dataSource.getConnection()) {
							
							Dataset expectedOutput = R2rmlTestCaseLib.loadOutput(testCase);
							
							Model r2rmlDocument = R2rmlTestCaseLib.loadMappingDocument(testCase);
							
	
//							Set<Quad> actualQuads = new LinkedHashSet<>();
							Dataset actualOutput = DatasetFactory.create();
							
//							RDFDataMgr.write(System.out, r2rmlDocument, RDFFormat.TURTLE_PRETTY);
							List<TriplesMap> tms = R2rmlLib.streamTriplesMaps(r2rmlDocument).collect(Collectors.toList());
							
							boolean usesJoin = tms.stream()
								.anyMatch(x -> x.getModel().listSubjectsWithProperty(RR.parentTriplesMap).toList().size() > 0);
															
							if (usesJoin) {
								System.err.println("Skipping mapping with join");
								continue;
							}
							
							boolean isOnSkipList = Arrays.asList(
									// "R2RMLTC0016b", // canonical double representation issue
									"R2RMLTC0020a", // Skipped because of encode-for-url application on value basis, mix of absolute and relative IRIs in column
									"R2RMLTC0019a" // Mixed absolute and relative IRIs
									// "R2RMLTC0012e" // canonical double representation issue: 
									).contains(testCaseId);
							if (isOnSkipList) {
								System.err.println("Skipping mapping");
								continue;
							}

							
//							if (!testCaseId.equals("R2RMLTC0016b")) {
//								continue;
//							}
							FunctionEnv env = createDefaultEnv();

							for (TriplesMap tm : tms) {
								
	//							Model closureModel = ResourceUtils.reachableClosure(tm);
	//							RDFDataMgr.write(System.out, closureModel, RDFFormat.TURTLE_PRETTY);
								
								LogicalTable lt = tm.getLogicalTable();							
								TriplesMapToSparqlMapping mapping = R2rmlImporter.read(tm);
								
								// System.out.println(mapping);

								Set<Var> usedVars = new HashSet<>();
								mapping.getVarToExpr().getExprs().values().stream().forEach(e -> ExprVars.varsMentioned(usedVars, e));
								Map<Var, String> usedVarToColumnName = usedVars.stream()
										.collect(Collectors.toMap(
												v -> v,
												v -> dequoteColumnName(v.getName())
										));
								
	
								String sqlQuery;
								if (lt.qualifiesAsBaseTableOrView()) {
									sqlQuery = "SELECT * FROM " + lt.asBaseTableOrView().getTableName();
								} else if (lt.qualifiesAsR2rmlView()) {
									sqlQuery = lt.asR2rmlView().getSqlQuery();
								} else {
									System.err.println("No logical table present");
									continue;
								}

								try (Statement stmt = conn.createStatement()) {
									ResultSet rs = stmt.executeQuery(sqlQuery);
									ResultSetMetaData rsmd = rs.getMetaData();
									
									RowMapper bindingMapper = JdbcUtils.createDefaultBindingMapper(rsmd, usedVarToColumnName); // .createBindingMapper(rs, usedVarToColumnName, new RowToNodeViaTypeManager());
									
									while (rs.next()) {
										 Binding b = bindingMapper.map(rs);
										 Binding effectiveBinding = mapping.evalVars(b, env);
										 
										 List<Quad> generatedQuads = mapping.evalQuads(effectiveBinding).collect(Collectors.toList()); 
										 
										 generatedQuads.forEach(actualOutput.asDatasetGraph()::add);
									}
								}
							}
							
							boolean isIso = isIsomorphic(expectedOutput, actualOutput);
							System.out.println("Assertion " + isIso);
							Assert.assertTrue(isIso);
							
//							System.out.println("Expected: " + expectedQuads);
//							System.out.println("Actual: " + actualQuads);
//							Set<Quad> missing = difference(expectedQuads, actualQuads, R2rmlTestSuiteProcessorH2::compareQuadsViaNodeValue);
//							Set<Quad> excessive = difference(actualQuads, expectedQuads, R2rmlTestSuiteProcessorH2::compareQuadsViaNodeValue);
//	//						
//							if (!missing.isEmpty()) {
//								System.err.println("Missing quads: " + missing);
//							}
//							if (!excessive.isEmpty()) {
//								System.err.println("Excessive quads: " + excessive);
//							}
//							
//							Assert.assertTrue("Non-empty set of missing quads", missing.isEmpty());
//							Assert.assertTrue("Non-empty set of excessive quads", excessive.isEmpty());
	//						Assert.assertEquals(expectedQuads, actualQuads);
	//						RDFDataMgr.write(System.out, actualOutput, RDFFormat.TRIG_PRETTY);
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

	
	public static boolean isIsomorphic(Dataset expected, Dataset actual) {
		boolean result;
		
		String everything = "SELECT ?g ?s ?p ?o { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } }";
//		String everything = "SELECT ?o { { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } } FILTER(isNumeric(?o))}";
		try (QueryExecution qea = QueryExecutionFactory.create(everything, expected);
			QueryExecution qeb = QueryExecutionFactory.create(everything, actual)) {
			
			ResultSetRewindable rsa = ResultSetFactory.copyResults(qea.execSelect());
			ResultSetRewindable rsb = ResultSetFactory.copyResults(qeb.execSelect());
									
			result = ResultSetCompare.equalsByValue(rsa, rsb);
			
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
	
	/**
	 * Difference of two collections w.r.t. a custom comparator.
	 * Runs in O(n^2)
	 * 
	 * @param <T>
	 * @param a
	 * @param b
	 * @param cmp
	 * @return
	 */
	public static <T> Set<T> difference(
			Collection<? extends T> a ,
			Collection<? extends T> b,
			Comparator<? super T> cmp) {
		Set<T> result = a.stream()
			.filter(ai -> b.stream().noneMatch(bi -> cmp.compare(ai, bi) == 0))
			.collect(Collectors.toSet());
		

//		System.out.println("Diff:");
//		result.forEach(System.out::println);
		return result;
	}
	
	
    public static int compareAlways(NodeValue nv1, NodeValue nv2)
    {
    	int result;
        try {
            result = NodeValue.compare(nv1, nv2);
        } catch (ExprNotComparableException ex) {
        	result =  NodeUtils.compareRDFTerms(nv1.asNode(), nv2.asNode());
        }
        return result;
    }

	/**
	 * Compare nodes such that e.g. "10"^^xsd:int is equal to "10"^^xsd:integer
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int compareNodesViaNodeValue(Node a, Node b) {
		NodeValue nva = NodeValue.makeNode(a);
		NodeValue nvb = NodeValue.makeNode(b);
		int result = compareAlways(nva, nvb);
		System.out.println(nva + (result == 0 ? " == " : " =/=") + nvb);
		return result;
	}
	
	public static int compareNodesEquiv(Node a, Node b) {
		int result = Quad.isDefaultGraph(a) && Quad.isDefaultGraph(b)
				? 0
				: compareNodesViaNodeValue(a, b);
		return result;
	}

	
	public static int compareQuadsViaNodeValue(Quad a, Quad b) {
		return compareQuads(a, b, R2rmlTestSuiteProcessorH2::compareNodesEquiv);
	}
	
	public static int compareQuads(Quad a, Quad b, Comparator<? super Node> nodeComparator) {
		int result = ComparisonChain.start()
			.compare(a.getGraph(), b.getGraph(), nodeComparator)
			.compare(a.getSubject(), b.getSubject(), nodeComparator)
			.compare(a.getPredicate(), b.getPredicate(), nodeComparator)
			.compare(a.getObject(), b.getObject(), nodeComparator)
			.result();
		
		return result;
	}
	
	public static String dequoteColumnName(String columnName) {
		String result = columnName.replaceAll("(^(\"))|((\")$)", "");
		return result;
	}

	public static FunctionEnv createDefaultEnv() {
        Context context = ARQ.getContext().copy() ;
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        FunctionEnv env = new ExecutionContext(context, null, null, null) ; 

        return env;
	}
}
