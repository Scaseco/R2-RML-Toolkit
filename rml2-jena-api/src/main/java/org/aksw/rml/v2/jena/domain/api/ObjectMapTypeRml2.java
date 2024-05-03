package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.common.IObjectMapType;


/**
 * Common base class for {@link ObjectMapRml2} and {@link RefObjectMapRml2}.
 * R2RML implicitly specifies the range of rr:objectMap to be the union of
 * rr:ObjectMap and rr:RefObjectMap however it does not give this union a name.
 *
 * @author raven
 *
 */
@ResourceView
public interface ObjectMapTypeRml2
    extends IObjectMapType, TermSpecRml2
{
    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    @Override
    default ObjectMapRml2 asTermMap() {
        return as(ObjectMapRml2.class);
    }
}
