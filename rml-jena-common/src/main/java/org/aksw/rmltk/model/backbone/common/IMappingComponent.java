package org.aksw.rmltk.model.backbone.common;


import org.apache.jena.rdf.model.Resource;

/**
 * Marker interface for R2RML classes in according with the R2RML specification:
 *
 * https://www.w3.org/TR/r2rml/#dfn-mapping-component
 *
 *   "The R2RML vocabulary also includes the following R2RML classes: [...]
 *    The members of these classes are collectively called mapping components."
 *
 * @author raven
 *
 */
public interface IMappingComponent
    extends Resource
{
}
