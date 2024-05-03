package org.aksw.rmltk.model.backbone.common;

/**
 * Common base class for {@link IObjectMap} and {@link IRefObjectMap}.
 * R2RML implicitly specifies the range of rr:objectMap to be the union of
 * rr:ObjectMap and rr:RefObjectMap however it does not give this union a name.
 *
 * @author raven
 *
 */
public interface IObjectMapType
    extends ITermSpec
{
    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    IObjectMap asTermMap();
}
