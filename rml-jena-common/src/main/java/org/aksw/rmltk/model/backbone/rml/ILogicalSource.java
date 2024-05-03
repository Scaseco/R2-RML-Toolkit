package org.aksw.rmltk.model.backbone.rml;

import org.aksw.rmltk.model.backbone.common.IAbstractSource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface ILogicalSource
    extends IAbstractSource
{
    RDFNode getSource();
    ILogicalSource setSource(RDFNode source);

    String getSourceAsString();
    Resource getSourceAsResource();

    String getIterator();
    ILogicalSource setIterator(String iterator);

    String getReferenceFormulationIri();
    ILogicalSource setReferenceFormulationIri(String iri);
}
