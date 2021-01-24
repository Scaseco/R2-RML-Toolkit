package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

public class H2Utils {

	
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
