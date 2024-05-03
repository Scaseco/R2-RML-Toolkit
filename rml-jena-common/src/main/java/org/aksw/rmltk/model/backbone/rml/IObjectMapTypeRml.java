package org.aksw.rmltk.model.backbone.rml;

import org.aksw.rmltk.model.backbone.common.IObjectMapType;

/**
 * Common base class for {@link IObjectMapRml} and {@link IRefObjectMapRml}.
 * R2RML implicitly specifies the range of rr:objectMap to be the union of
 * rr:ObjectMap and rr:RefObjectMap however it does not give this union a name.
 *
 * @author raven
 *
 */
public interface IObjectMapTypeRml
    extends IObjectMapType, ITermSpecRml
{
    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    @Override IObjectMapRml asTermMap();
}
