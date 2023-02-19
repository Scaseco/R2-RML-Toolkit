package org.aksw.rml.jena.service;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.rml.jena.impl.RmlLib;
import org.aksw.rml.model.LogicalSource;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.syntax.Element;

public class InitRmlService {
    public static void registerServiceRmlSource(ServiceExecutorRegistry registry) {
        registry.addSingleLink((opExecute, opOriginal, binding, execCxt, chain) -> {
            opExecute.getService();
            Op subOp = opExecute.getSubOp();
            Query query = OpAsQuery.asQuery(subOp);
            Element elt = query.getQueryPattern();
            Graph graph = ElementUtils.toGraph(elt);
            Model model = ModelFactory.createModelForGraph(graph);
            LogicalSource logicalSource = RmlLib.getLogicalSource(model);
            // ReferenceFormulationRegistry reg = ReferenceFormulationRegistry.get(execCxt.getContext());

            // FIXME finish implementation: We need to create a QueryIter from the logicalSource

            return chain.createExecution(opExecute, opOriginal, binding, execCxt);
        });
    }
}
