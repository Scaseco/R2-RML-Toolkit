package org.aksw.rmltk.model.r2rml;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.common.IObjectMap;


@ResourceView
public interface ObjectMap
    extends IObjectMap, ObjectMapType, TermMap
{
}
