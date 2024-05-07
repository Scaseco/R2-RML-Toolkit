package org.aksw.rml.jena.service;

import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

/** Interface for evaluating sources (specified by an RML LogicalSource model) to bindings */
public interface RmlSourceProcessor {
    QueryIterator eval(ILogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt);
}
