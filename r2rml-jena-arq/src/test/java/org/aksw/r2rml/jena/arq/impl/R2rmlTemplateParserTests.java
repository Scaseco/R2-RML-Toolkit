package org.aksw.r2rml.jena.arq.impl;

import org.apache.jena.sparql.expr.Expr;
import org.junit.Assert;
import org.junit.Test;

public class R2rmlTemplateParserTests {
    @Test
    public void testSimple() {
        Expr expr = R2rmlTemplateLib.parse("http://data.example.com/department/{DEPTNO}");
        System.out.println(expr);
    }

    @Test
    public void testEscaping() {
        Expr expr = R2rmlTemplateLib.parse("\\{\\{\\{ \\\\o/ {TITLE} \\\\o/ \\}\\}\\}");
        System.out.println(expr);
    }

    @Test
    public void testSimpleRoundTrip() {
        String input = "http://data.example.com/department/{DEPTNO}";
        Expr expr = R2rmlTemplateLib.parse(input);
        String output = R2rmlTemplateLib.deparse(expr);

        Assert.assertEquals(input, output);
    }

    @Test
    public void testEscapingRoundTrip() {
        String input = "\\{\\{\\{ \\\\o/ {TITLE} \\\\o/ \\}\\}\\}";
        Expr expr = R2rmlTemplateLib.parse(input);
        String output = R2rmlTemplateLib.deparse(expr);

        Assert.assertEquals(input, output);
    }
}
