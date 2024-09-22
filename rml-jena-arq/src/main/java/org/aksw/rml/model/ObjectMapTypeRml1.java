package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.rml.IObjectMapTypeRml;

@ResourceView
public interface ObjectMapTypeRml1
    extends IObjectMapTypeRml, TermSpecRml1
{
    /**
     * Obtain an ObjectMap view of this resource.
     * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
     *
     * @return
     */
    @Override
    default ObjectMapRml1 asTermMap() {
        return as(ObjectMapRml1.class);
    }
}
