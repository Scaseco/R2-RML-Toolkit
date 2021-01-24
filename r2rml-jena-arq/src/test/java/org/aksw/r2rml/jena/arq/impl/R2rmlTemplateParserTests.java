package org.aksw.r2rml.jena.arq.impl;

import org.apache.jena.sparql.expr.Expr;
import org.junit.Test;

public class R2rmlTemplateParserTests {
	@Test
	public void testSimple() {
		Expr expr = R2rmlTemplateParser.parseTemplate("http://data.example.com/department/{DEPTNO}");
		System.out.println(expr);
	}

	@Test
	public void testEscaping() {
		Expr expr = R2rmlTemplateParser.parseTemplate("\\{\\{\\{ \\\\o/ {TITLE} \\\\o/ \\}\\}\\}");
		System.out.println(expr);
	}
}
