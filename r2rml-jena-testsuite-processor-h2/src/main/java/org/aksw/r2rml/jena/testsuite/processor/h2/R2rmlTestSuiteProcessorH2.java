package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.sql.DataSource;

import org.aksw.r2rml.jena.arq.impl.R2rmlImporter;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLib;
import org.aksw.r2rml.jena.testsuite.R2rmlTestCaseLoader;
import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

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
										
					Dataset expectedOutput = R2rmlTestCaseLib.loadOutput(testCase);
					Model r2rmlDocument = R2rmlTestCaseLib.loadMappingDocument(testCase);
					
					Collection<TriplesMapToSparqlMapping> mappings = new R2rmlImporter().read(r2rmlDocument);
					
					for (TriplesMapToSparqlMapping m : mappings) {
						System.out.println("Mapping: " + m);
						System.out.println(m.getTemplate());
						System.out.println(m.getVarToExpr());
					}
					
				} finally {
					if (dataSource != null) {
						shutdownH2(dataSource);
					}
				}
				
				
			}
		}
		
		System.out.println("yay");
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
