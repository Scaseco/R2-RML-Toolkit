package org.aksw.rml.jena.ref.impl;


import java.util.function.Consumer;

import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.rml.jena.impl.NorseRmlTerms;
import org.aksw.rml.jena.impl.ReferenceFormulation;
import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

public abstract class ReferenceFormulationViaServiceBase
    implements ReferenceFormulation
{
    @Override
    public Element source(ILogicalSource logicalSource, Var sourceVar) {
        BasicPattern bgp = new BasicPattern();

        // Replace the logical source with a constant in order to make
        // equality checks easier
        Node s = NodeFactory.createURI(NorseRmlTerms.RML_SOURCE_SERVICE_IRI);

        // TODO Include database config
        RDFNode source = logicalSource.getSource();
        D2rqDatabase db = source.as(D2rqDatabase.class);


        // Only add the immediate triples
        logicalSource.listProperties()
            .mapWith(t -> Triple.create(s, t.getPredicate().asNode(), addObject(t.getObject(), bgp::add)))
            .forEach(bgp::add);
        bgp.add(Triple.create(s, NodeFactory.createURI(NorseRmlTerms.output), sourceVar));
        ElementService result = new ElementService(NorseRmlTerms.RML_SOURCE_SERVICE_IRI, new ElementTriplesBlock(bgp));
        return result;
    }

    private Node addObject(RDFNode node, Consumer<Triple> action) {
        if (node.isResource()) {
            Model model = ModelFactory.createDefaultModel();
            node.asResource().listProperties()
                    .mapWith(t -> Triple.create(t.getSubject().asNode(), t.getPredicate().asNode(), addObject(t.getObject(), action)))
                    .forEach(action);
        }
        return node.asNode();
    }

    @Override
    public abstract Expr reference(Var itemVar, String expr);
}
