package org.aksw.rml.jena.ref.impl;

import java.util.Arrays;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.rml.jena.impl.ReferenceFormulation;
import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;

public class ReferenceFormulationCsvViaCsvParse
    implements ReferenceFormulation
{
    @Override
    public Element source(ILogicalSource logicalSource, Var sourceVar) {
        String source = logicalSource.getSourceAsString();
        Node csvParse = NodeFactory.createURI("http://jsa.aksw.org/fn/csv/parse");
        Element result = ElementUtils.createElementTriple(NodeFactory.createURI(source), csvParse, sourceVar);
        return result;
    }

    @Override
    public Expr reference(Var itemVar, String expr) {
        String jsonPath = "http://jsa.aksw.org/fn/json/path";
        return new E_Function(jsonPath, ExprList.create(Arrays.asList(new ExprVar(itemVar), NodeValue.makeString("$." + expr))));
    }
}
