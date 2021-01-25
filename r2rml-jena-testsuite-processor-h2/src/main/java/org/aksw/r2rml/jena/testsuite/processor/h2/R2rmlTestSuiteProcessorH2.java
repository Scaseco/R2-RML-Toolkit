package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import org.aksw.r2rml.jena.jdbc.api.RowToBinding;
import org.aksw.r2rml.jena.jdbc.impl.RowToNodeViaTypeManager;
import org.aksw.r2rml.jena.jdbc.util.JdbcUtils;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLib;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLoader;
import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.ext.com.google.common.collect.ComparisonChain;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.NodeUtils;

import junit.framework.Assert;


public class R2rmlTestSuiteProcessorH2 {
	public static void main(String[] args) throws SQLException, IOException {
		// Collection<Database> databases = R2rmlTestCaseLoader.importDatabases();
		Collection <R2rmlTestCase> testCases = R2rmlTestCaseLoader.importTestCases();
				
		for (R2rmlTestCase testCase : testCases) {
			Database database = testCase.getDatabase();
						
			if (database != null) {
				DataSource dataSource = null;
				try {
					dataSource = H2Utils.prepareDataSource(database);
			
					try (Connection conn = dataSource.getConnection()) {
						
						Dataset expectedOutput = R2rmlTestCaseLib.loadOutput(testCase);
						
						Set<Quad> expectedQuads = new LinkedHashSet<>();
						expectedOutput.asDatasetGraph().find(null, null, null, null)
							.forEachRemaining(expectedQuads::add);
						
						Model r2rmlDocument = R2rmlTestCaseLib.loadMappingDocument(testCase);
						

						Set<Quad> actualQuads = new LinkedHashSet<>();
						
						RDFDataMgr.write(System.out, r2rmlDocument, RDFFormat.TURTLE_PRETTY);
						List<TriplesMap> tms = R2rmlLib.streamTriplesMaps(r2rmlDocument).collect(Collectors.toList());
						
						for (TriplesMap tm : tms) {
							
//							Model closureModel = ResourceUtils.reachableClosure(tm);
//							RDFDataMgr.write(System.out, closureModel, RDFFormat.TURTLE_PRETTY);
							
							LogicalTable lt = tm.getLogicalTable();							
							TriplesMapToSparqlMapping mapping = R2rmlImporter.read(tm);
							
							System.out.println(mapping);
							
							Set<Var> usedVars = new HashSet<>();
							mapping.getVarToExpr().values().stream().forEach(e -> ExprVars.varsMentioned(usedVars, e));
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
								
								RowToBinding bindingMapper = JdbcUtils.createBindingMapper(rs, usedVarToColumnName, new RowToNodeViaTypeManager());
								
								while (rs.next()) {
									 Binding b = bindingMapper.map(rs);
									 mapping.evalQuads(b).forEach(actualQuads::add);
								}
							}
						}
						
						Set<Quad> missing = difference(expectedQuads, actualQuads, R2rmlTestSuiteProcessorH2::compareQuadsViaNodeValue);
						Set<Quad> excessive = difference(actualQuads, expectedQuads, R2rmlTestSuiteProcessorH2::compareQuadsViaNodeValue);
//						
						if (!missing.isEmpty()) {
							System.err.println("Missing quads: " + missing);
						}
						if (!excessive.isEmpty()) {
							System.err.println("Excessive quads: " + excessive);
						}
						
						Assert.assertTrue("Non-empty set of missing quads", missing.isEmpty());
						Assert.assertTrue("Non-empty set of excessive quads", excessive.isEmpty());
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
//		System.out.println(nva + (result == 0 ? " == " : " =/=") + nvb);
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
}
