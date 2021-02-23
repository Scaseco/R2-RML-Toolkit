package org.aksw.r2rml.sql.transform;

import java.util.concurrent.Callable;
import java.util.function.Function;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.r2rml.jena.sql.transform.SqlParseException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class SqlUtils {
	
	/**
	 * Utility method to hide JSQL from the public API by catching {@link JSQLParserException}
	 * and rethrowing it as {@link SqlParseException}
	 */
	public static <T> T wrapJsqlException(Callable<T> callable) throws SqlParseException {
		T result;
		try {
			result = callable.call();
		} catch (JSQLParserException e) {
			throw new SqlParseException(e);
		} catch (Exception e) {
			// Control flow should not come here
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	/** Public utility method for harmonizing identifiers w.r.t. an sql codec */
	public static String harmonizeIdentifiers(String sqlStr, SqlCodec sqlCodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.harmonizeIdentifiers(sqlStr, sqlCodec));
	}
	

	/**
	 * 
	 * @param sqlStr An SQL string
	 * @param targetSchemaName The schema name by which to qualify table names
	 * @param replaceAll Also replace existing non-null schema names
	 * @return
	 * @throws SqlParseException
	 */
	public static String setSchemaForTables(String sqlStr, String targetSchemaName, boolean replaceAll) throws SqlParseException {
		return transformTableSchemas(sqlStr, name -> replaceAll || name == null ? targetSchemaName : name);
	}

	public static String transformTableSchemas(String sqlStr, Function<String, String> schemaTransform) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.applySchemaTransform(sqlStr, schemaTransform));
	}

	public static String replaceIdentifier(String identifier, String oldEscapeChar, String newEscapeChar) {
		return identifier.replace(oldEscapeChar, newEscapeChar);
	}

	public static String replaceQueryIdentifiers(String query, String oldEscapeChar, String newEscapeChar)
			throws SqlParseException {
		try {
			return replaceQueryIdentifiersRaw(query, oldEscapeChar, newEscapeChar);
		} catch (JSQLParserException e) {
			throw new SqlParseException(e);
		}
	}

	/**
	 * The replace method with the JSQLParseException throws declaration
	 */
	private static String replaceQueryIdentifiersRaw(String query, String oldEscapeChar, String newEscapeChar)
			throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(query);
		Select selectStatement = (Select) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Table table) {
				table.setName(table.getName().replace(oldEscapeChar, newEscapeChar));
			}
		};
		selectStatement.accept(tablesNamesFinder);
		String result = statement.toString();
		return result;
	}

}
