package org.aksw.r2rml.sql.transform;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.commons.codec.entity.util.EntityCodecUtils;
import org.aksw.commons.sql.codec.api.SqlCodec;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class JSqlUtils {
	
	public static Table parseTableName(String tableName) throws JSQLParserException {
		String dummy = "SELECT * FROM " + tableName;		
		Table tableRef[] = {null};
		
		Statement statement = CCJSqlParserUtil.parse(dummy);
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Table table) {
				tableRef[0] = table;
			}
		};
		tablesNamesFinder.getTableList(statement);

		Table table = tableRef[0];
		Objects.requireNonNull(table, "Could not parse colmn " + tableName + " in context " + dummy);

		return table;
	}

	
	public static Column parseColumnName(String columnName) throws JSQLParserException {
		String dummy = "SELECT " + columnName + " FROM dummyTable";	
		Column columnRef[] = {null};
		
		Statement statement = CCJSqlParserUtil.parse(dummy);
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Column column) {
				columnRef[0] = column;
			}
		};
		tablesNamesFinder.getTableList(statement);

		Column column = columnRef[0];
		Objects.requireNonNull(column, "Could not parse colmn " + columnName + " in context " + dummy);
		return column;
	}

	
	public static Table harmonizeTable(Table table, SqlCodec sqlCodec) {
		String harmonizedTableName = EntityCodecUtils.harmonize(table.getName(), sqlCodec::forTableName);
		table.setName(harmonizedTableName);

		String schemaName = table.getSchemaName();
		
		if (schemaName != null) {
			String harmonizedSchemaName = EntityCodecUtils.harmonize(schemaName, sqlCodec::forSchemaName);
			table.setSchemaName(harmonizedSchemaName);
		}

		Alias alias = table.getAlias();
		if (alias != null) {
//			List<AliasColumn> aliasColumns = alias.getAliasColumns();
//			if (aliasColumns != null) {
//				for (AliasColumn aliasColumn : aliasColumns) {
//					// columnAlias.
//					// System.out.println(aliasColumn);
//				}
//			}
			
			String harmonizedAlias = EntityCodecUtils.harmonize(alias.getName(), sqlCodec::forAlias);
			alias.setName(harmonizedAlias);
			table.setAlias(alias);
		}
		
		return table;
	}
	
	public static Table reencodeTable(Table table, SqlCodec sqlDecodec, SqlCodec sqlEncodec) {
		String reencodedTableName = EntityCodecUtils.reencode(table.getName(), sqlDecodec::forTableName, sqlEncodec::forTableName);
		table.setName(reencodedTableName);

		String schemaName = table.getSchemaName();
		
		if (schemaName != null) {
			String reencodedSchemaName = EntityCodecUtils.reencode(schemaName, sqlDecodec::forSchemaName, sqlEncodec::forSchemaName);
			table.setSchemaName(reencodedSchemaName);
		}

		Alias alias = table.getAlias();
		if (alias != null) {
			String reencodedAlias = EntityCodecUtils.reencode(alias.getName(), sqlDecodec::forAlias, sqlEncodec::forAlias);
			alias.setName(reencodedAlias);
			table.setAlias(alias);
		}
		
		return table;
	}
	
	
	public static Column harmonizeColumn(Column column, SqlCodec sqlCodec) {
		String harmonizedName = EntityCodecUtils.harmonize(column.getColumnName(), sqlCodec::forColumnName);
		column.setColumnName(harmonizedName);
		return column;
	}

	public static Column reencodeColumn(Column column, SqlCodec decodec, SqlCodec encodec) {
		String harmonizedName = EntityCodecUtils.reencode(
				column.getColumnName(),
				decodec::forColumnName,
				encodec::forColumnName);
		column.setColumnName(harmonizedName);
		return column;
	}

	
	public static String harmonizeIdentifiers(String sqlSelectQueryStr, SqlCodec sqlCodec) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sqlSelectQueryStr);

		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Table table) {
				harmonizeTable(table, sqlCodec);
			}
			
			@Override
			public void visit(Column tableColumn) {
				harmonizeColumn(tableColumn, sqlCodec);
			}
		};

		tablesNamesFinder.getTableList(statement);
		String result = statement.toString();
		return result;
	}
	
	
	public static String reencodeIdentifiers(String sqlSelectQueryStr, SqlCodec sqlDecodec, SqlCodec sqlEncodec) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sqlSelectQueryStr);

		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Table table) {
				reencodeTable(table, sqlDecodec, sqlEncodec);
			}
			
			@Override
			public void visit(Column tableColumn) {
				reencodeColumn(tableColumn, sqlDecodec, sqlEncodec);
			}
		};

		tablesNamesFinder.getTableList(statement);
		String result = statement.toString();
		return result;
	}

	
	public static String applySchemaTransform(String query, Function<String, String> schemaTransform) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(query);

		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Table table) {
				String oldSchemaName = table.getSchemaName();
				String newSchemaName = schemaTransform.apply(oldSchemaName);
				table.setSchemaName(newSchemaName);
			}
		};

		tablesNamesFinder.getTableList(statement);
		String result = statement.toString();
		return result;
	}
}
