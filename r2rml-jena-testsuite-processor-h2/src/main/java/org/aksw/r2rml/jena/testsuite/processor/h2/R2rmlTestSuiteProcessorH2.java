package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.aksw.r2rml.jena.arq.impl.R2rmlImporter;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLib;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLoader;
import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.util.ResourceUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

import com.google.common.collect.Sets;

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
					dataSource = prepareDataSource(database);
			
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
								
								BindingMapper bindingMapper = createBindingMapper(rs, usedVarToColumnName, new NodeMapperViaTypeManager());
								
								while (rs.next()) {
									 Binding b = bindingMapper.map(rs);
									 mapping.evalQuads(b).forEach(actualQuads::add);
								}
							}
						}
						
//						Set<Quad> missing = Sets.difference(expectedQuads, actualQuads);
//						Set<Quad> excessive = Sets.difference(actualQuads, expectedQuads);
						
						Assert.assertEquals(expectedQuads, actualQuads);
						
						
//						RDFDataMgr.write(System.out, actualOutput, RDFFormat.TRIG_PRETTY);
					}
					
				} finally {
					if (dataSource != null) {
						shutdownH2(dataSource);
					}
				}
				
				
			}
		}
		
	}
	
	public static String dequoteColumnName(String columnName) {
		String result = columnName.replaceAll("(^(\"))|((\")$)", "");
		return result;
	}
	
		
	public static BindingMapper createBindingMapper(
			ResultSet rs,
			// Set<Var> usedVars,
			Map<Var, String> usedVarToColumnName,
			NodeMapper nodeMapper
			) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		// Iterate the columns in the given order and map those columns
		// that have a variable in usedVars
		Set<String> availableColumns = new LinkedHashSet<>();
		Map<Var, Integer> colNameToIdx = new LinkedHashMap<>();

		Map<String, Var> columnNameToVar = HashBiMap.create(usedVarToColumnName).inverse();
		for (int i = 1; i <= columnCount; ++i) {
			String columnName = rsmd.getColumnName(i);			
			availableColumns.add(columnName);
			
			Var usedVar = columnNameToVar.get(columnName);
//			if (usedVars.contains(cv)) {
			if (usedVar != null) {
				colNameToIdx.put(usedVar, i);
			}
		}
		
		Set<Var> invalidRefs = Sets.difference(usedVarToColumnName.keySet(), colNameToIdx.keySet());
		if (!invalidRefs.isEmpty()) {
			throw new RuntimeException("The following non-existent columns are referenced: "
					+ invalidRefs + "; available: " + availableColumns);
		}
		
		int n = colNameToIdx.size();
		int[] colIdxs = new int[n];
		Var[] vars = new Var[n];

		int i = 0;
		for (Entry<Var, Integer> e : colNameToIdx.entrySet()) {
			vars[i] = e.getKey();
			colIdxs[i] = e.getValue();
			++i;
		}
	
		BindingMapper result = new ResultSetToBindingMapper(colIdxs, vars, nodeMapper);
		return result;
	}
	
	
	public static DataSource prepareDataSource(Database database) throws SQLException, IOException {

		DataSource dataSource = createDefaultDatabase("test");

		try (Connection conn = dataSource.getConnection()) {
			
			String sqlScriptFile = database.getSqlScriptFile();
	
			if (sqlScriptFile != null) {
				try (InputStream in = R2rmlTestSuiteProcessorH2.class.getClassLoader().getResourceAsStream(sqlScriptFile)) {
					RunScript.execute(conn, new InputStreamReader(in));
					System.out.println("Loaded script: " + database.getIdentifier());
				}
			}		

		}


		return dataSource;
	}

	public static DataSource createDefaultDatabase(String name) {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		dataSource.setUser("sa");
		dataSource.setPassword("");

		return dataSource;
	}

	public static void shutdownH2(DataSource dataSource) throws SQLException {
		try (Connection conn = dataSource.getConnection()) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("SHUTDOWN");
			}
		}
	}
}
