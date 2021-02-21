package org.aksw.r2rml.jena.sql.transform;

import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class SqlUtils {
	public static String qualifyTableNames(String sqlStr, List<String> qualifiers) throws JSQLParserException {
		Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM MY_TABLE1");
		Select selectStmt = (Select) stmt;
		
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
			@Override
			protected String extractTableName(Table table) {
				String rawName = super.extractTableName(table);
				table.setName("yay-" + rawName);
				// TODO Auto-generated method stub
				return rawName;
			}
		};
		List<String> tableList = tablesNamesFinder.getTableList(selectStmt);
		String result = stmt.toString();
		
		return result;
	}
}
