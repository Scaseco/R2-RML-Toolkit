package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface LogicalSourceRml1
    extends ILogicalSource
{
    @Iri(RmlTerms.source)
    @Override RDFNode getSource();
    @Override LogicalSourceRml1 setSource(RDFNode source);

    @Override
    default String getSourceAsString() {
        return getSource().asLiteral().getLexicalForm();
    }

    @Override
    default Resource getSourceAsResource() {
        return getSource().asResource();
    }

    @Iri(RmlTerms.iterator)
    @Override String getIterator();
    @Override LogicalSourceRml1 setIterator(String iterator);

    @Iri(RmlTerms.referenceFormulation)
    @IriType
    @Override String getReferenceFormulationIri();
    @Override LogicalSourceRml1 setReferenceFormulationIri(String iri);
}
