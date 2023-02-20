package org.aksw.rml.jena.service;

import org.aksw.rml.model.LogicalSource;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

/** Interface for evaluating sources (specified by an RML LogicalSource model) to bindings */
public interface RmlSourceProcessor {
    QueryIterator eval(LogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt);
}
