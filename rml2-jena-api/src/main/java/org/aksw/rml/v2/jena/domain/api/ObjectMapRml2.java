package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.rml.IObjectMapRml;


@ResourceView
public interface ObjectMapRml2
    extends IObjectMapRml, ObjectMapTypeRml2, TermMapRml2
{
}
