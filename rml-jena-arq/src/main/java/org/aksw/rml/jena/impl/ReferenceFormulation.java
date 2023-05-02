package org.aksw.rml.jena.impl;

import org.aksw.rml.model.LogicalSource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;

/**
 * Interface for turning logical sources into (syntactic) SPARQL
 * elements (i.e. graph patterns) and references into SPARQL expressions
 */
public interface ReferenceFormulation {
    /**
     * <pre>{@code
     *   <source.xml> xml:parse ?xmlDoc
     * }</pre>
     *
     * @param logicalSource A RML logical source specification
     * @param sourceVar A SPARQL variable that represents the source.
     * @return A graph pattern for loading the source
     */
    Element source(LogicalSource logicalSource, Var sourceVar);

    /**
     * If true then the source is considered document, otherwise a stream of items.
     * Iteration using {@link #iterate(Var, String, Var)} only applies to documents.
     */
    // boolean isDocument();

    /** Create a graph pattern that turns a document into a sequence of items
     * based on the given expression. This method is not needed for stream sources. */
    // Element iterate(Var docVar, String expr, Var itemVar);

    /** Access attributes of an item via the given expression */
    Expr reference(Var itemVar, String expr);
}
