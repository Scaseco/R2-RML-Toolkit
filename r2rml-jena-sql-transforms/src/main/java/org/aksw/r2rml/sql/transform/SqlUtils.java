package org.aksw.r2rml.sql.transform;

import java.util.concurrent.Callable;
import java.util.function.Function;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.r2rml.jena.sql.transform.SqlParseException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * Sql utility methods that are sufficiently generic such that abstraction from JSQL seems reasonable.
 * Most methods delegate to JsqlUtils but this may change anytime.
 * */
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
	public static String harmonizeQueryString(String sqlStr, SqlCodec sqlCodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.harmonizeIdentifiers(sqlStr, sqlCodec));
	}

	public static String harmonizeTableName(String tableName, SqlCodec sqlCodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.harmonizeTable(JSqlUtils.parseTableName(tableName), sqlCodec)).toString();
	}
	
	public static String harmonizeColumnName(String columnName, SqlCodec sqlCodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.harmonizeColumn(JSqlUtils.parseColumnName(columnName), sqlCodec)).toString();
	}


	
	public static String reencodeQueryString(String sqlStr, SqlCodec sqlDecodec, SqlCodec sqlEncodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.reencodeIdentifiers(sqlStr, sqlDecodec, sqlEncodec));
	}

	public static String reencodeTableName(String tableName, SqlCodec sqlDecodec, SqlCodec sqlEncodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.reencodeTable(JSqlUtils.parseTableName(tableName), sqlDecodec, sqlEncodec)).toString();
	}
	
	public static String reencodeColumnName(String columnName, SqlCodec sqlDecodec, SqlCodec sqlEncodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.reencodeColumn(JSqlUtils.parseColumnName(columnName), sqlDecodec, sqlEncodec)).toString();
	}

	
	public static String reencodeQueryStringDefault(String sqlStr, SqlCodec sqlEncodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.reencodeIdentifiers(
				sqlStr,
				SqlCodecUtils.createSqlCodecDefault(),
				sqlEncodec));
	}

	public static String reencodeTableNameDefault(String tableName, SqlCodec sqlEncodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.reencodeTable(
				JSqlUtils.parseTableName(tableName),
				SqlCodecUtils.createSqlCodecDefault(),
				sqlEncodec)).toString();
	}
	
	public static String reencodeColumnNameDefault(String columnName, SqlCodec sqlEncodec) throws SqlParseException {
		return wrapJsqlException(() -> JSqlUtils.reencodeColumn(
				JSqlUtils.parseColumnName(columnName),
				SqlCodecUtils.createSqlCodecDefault(),
				sqlEncodec)).toString();
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
