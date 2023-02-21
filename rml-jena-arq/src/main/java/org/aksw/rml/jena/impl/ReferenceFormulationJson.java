package org.aksw.rml.jena.impl;

import java.util.Arrays;

import org.aksw.rml.model.LogicalSource;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;

public class ReferenceFormulationJson
    implements ReferenceFormulation
{
    @Override
    public Element source(LogicalSource logicalSource, Var sourceVar) {
        String templateStr = String.join("\n",
            "BIND(<http://jsa.aksw.org/fn/url/text>(<URL>) AS ?SRC_text)",
            "BIND(STRDT(?SRC_text, <http://www.w3.org/2001/XMLSchema#json>) AS ?SRC_doc)",
            "BIND(<http://jsa.aksw.org/fn/json/path>(?SRC_doc, 'ITER') AS ?SRC_items)",
            "?SRC_items <http://jsa.aksw.org/fn/json/unnest> ?SRC"
        );

        String str = templateStr
                .replaceAll("URL", logicalSource.getSource())
                .replaceAll("ITER", logicalSource.getIterator())
                .replaceAll("SRC", sourceVar.getName());

        Element result = QueryFactory.createElement(str);
        return result;
//        Element result = ElementUtils.groupIfNeeded(
//            new ElementBind(sourceVar, new E_StrDatatype(
//                new E_Function(urlText, new ExprList(NodeValue.makeString(source))),
//                NodeValue.makeNode(NodeFactory.createURI("http://www.w3.org/2001/XMLSchema#json")))),
//        );
    }

    @Override
    public Expr reference(Var itemVar, String expr) {
        String jsonPath = "http://jsa.aksw.org/fn/json/path";
        return new E_Function(jsonPath, ExprList.create(Arrays.asList(new ExprVar(itemVar), new ExprVar("$['" + expr.replaceAll("'", "\\'") + "]"))));
    }
}
