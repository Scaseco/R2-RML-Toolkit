package org.aksw.r2rml.sql.transform;

import java.util.List;
import java.util.function.Function;

import org.aksw.commons.codec.entity.util.EntityCodecUtils;
import org.aksw.commons.sql.codec.api.SqlCodec;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Alias.AliasColumn;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class JSqlUtils {
	public static String harmonizeIdentifiers(String sqlSelectQueryStr, SqlCodec sqlCodec) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(sqlSelectQueryStr);

		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			public void visit(Table table) {
				String harmonizedTableName = EntityCodecUtils.harmonize(table.getName(), sqlCodec::forTableName);
				table.setName(harmonizedTableName);

				String harmonizedSchemaName = EntityCodecUtils.harmonize(table.getSchemaName(), sqlCodec::forSchemaName);
				table.setSchemaName(harmonizedSchemaName);

				Alias alias = table.getAlias();
				if (alias != null) {
					List<AliasColumn> aliasColumns = alias.getAliasColumns();
					if (aliasColumns != null) {
						for (AliasColumn aliasColumn : aliasColumns) {
							//columnAlias.
							System.out.println(aliasColumn);
						}
					}
					
					String harmonizedAlias = EntityCodecUtils.harmonize(alias.getName(), sqlCodec::forAlias);
					alias.setName(harmonizedAlias);
					table.setAlias(alias);
				}

			}
			
			@Override
			public void visit(Column tableColumn) {
				String harmonizedName = EntityCodecUtils.harmonize(tableColumn.getColumnName(), sqlCodec::forColumnName);
				tableColumn.setColumnName(harmonizedName);
			}
		};

		statement.accept(tablesNamesFinder);
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

		statement.accept(tablesNamesFinder);
		String result = statement.toString();
		return result;
	}
}
