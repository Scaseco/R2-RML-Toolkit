package org.aksw.rmltk.model.backbone.rml;


import org.aksw.rmltk.model.backbone.common.ITermMap;

/**
 * A TermMap with all attributes according to the R2RML specification.
 *
 * @author Claus Stadler
 *
 */
public interface ITermMapRml
    extends ITermMap
{
    String getReference();
    ITermMapRml setReference(String reference);
}
