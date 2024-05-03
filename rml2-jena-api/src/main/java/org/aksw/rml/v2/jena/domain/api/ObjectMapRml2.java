package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.common.IObjectMap;


@ResourceView
public interface ObjectMapRml2
    extends IObjectMap, ObjectMapTypeRml2, TermMapRml2
{
}
