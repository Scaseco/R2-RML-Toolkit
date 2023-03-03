package org.aksw.rml.jena.ref.impl;

import org.aksw.fno.model.Fno;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.rml.jena.impl.ReferenceFormulation;
import org.aksw.rml.jena.impl.SparqlX_Rml_Terms;
import org.aksw.rml.model.LogicalSource;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

/**
 *
 */
public class ReferenceFormulationTarql
    implements ReferenceFormulation
{
    @Override
    public Element source(LogicalSource logicalSource, Var sourceVar) {
        BasicPattern bgp = new BasicPattern();

        // Replace the logical source with a constant in order to make
        // equality checks easier
        Node s = NodeFactory.createURI(SparqlX_Rml_Terms.RML_SOURCE_SERVICE_IRI);

        // Only add the immediate triples
        logicalSource.listProperties()
            .mapWith(stmt -> stmt.asTriple())
            .mapWith(t -> Triple.create(s, t.getPredicate(), t.getObject()))
            .forEach(bgp::add);
        // GraphUtil.findAll(logicalSource.getModel().getGraph()).forEach(bgp::add);
        bgp.add(Triple.create(s, Fno.returns.asNode(), sourceVar));
        ElementService result = new ElementService(SparqlX_Rml_Terms.RML_SOURCE_SERVICE_IRI, new ElementTriplesBlock(bgp));
        return result;
    }

    @Override
    public Expr reference(Var itemVar, String expr) {
        Var var = VarUtils.safeVar(expr);
        return new ExprVar(var);
    }
}
