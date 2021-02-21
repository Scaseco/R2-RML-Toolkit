package org.aksw.r2rml.jena.sql.transform;

import java.util.Arrays;

import net.sf.jsqlparser.JSQLParserException;

public class R2rmlSqlLib {
	public static void main(String[] args) throws JSQLParserException {
		String result = SqlUtils.qualifyTableNames("SELECT * FROM MY_TABLE1", Arrays.asList("a", "b"));
		System.out.println(result);
	}
}
