package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;


/**
 * Common base class for {@link ObjectMap} and {@link RefObjectMap}.
 * R2RML implicitly specifies the range of rr:objectMap to be the union of
 * rr:ObjectMap and rr:RefObjectMap however it does not give this union a name.
 *
 * @author raven
 *
 */
@ResourceView
public interface ObjectMapType
    extends TermSpec
{
    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    @Override
    default ObjectMap asTermMap() {
        return as(ObjectMap.class);
    }
}
