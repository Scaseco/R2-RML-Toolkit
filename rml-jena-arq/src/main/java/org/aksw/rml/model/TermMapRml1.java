package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.r2rml.jena.vocab.RR;
import org.aksw.rmltk.model.backbone.common.IDatatypeMap;
import org.aksw.rmltk.model.backbone.rml.ITermMapRml;
import org.aksw.rmltk.model.r2rml.TermMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface TermMapRml1
    extends ITermMapRml, TermSpecRml1 // , TermSpec
{
    @Iri(R2rmlTerms.termType)
    @Override Resource getTermType();
    @Override TermMap setTermType(Resource termType);

    @Iri(RmlTerms.reference)
    @Override String getReference();
    @Override TermMapRml1 setReference(String reference);

    @Iri(R2rmlTerms.column)
    @Override String getColumn();
    @Override TermMapRml1 setColumn(String columnName);

    @Iri(R2rmlTerms.language)
    @Override String getLanguage();
    @Override TermMapRml1 setLanguage(String language);

    @Iri(R2rmlTerms.datatype)
    @Override Resource getDatatype();
    @Override TermMapRml1 setDatatype(Resource datatype);

    @Override
    default IDatatypeMap getDatatypeMap() {
        return null;
    }

    @Override
    default TermMapRml1 setDatatypeMap(IDatatypeMap datatypeMap) {
        if (datatypeMap != null) {
            throw new UnsupportedOperationException("DatatypeMap is not supported in this model");
        }
        return this;
    }

    @Iri(R2rmlTerms.constant)
    @Override RDFNode getConstant();
    @Override TermMapRml1 setConstant(RDFNode constant);

    @Iri(R2rmlTerms.template)
    @Override String getTemplate();
    @Override TermMapRml1 setTemplate(String template);

    @Iri(R2rmlTerms.inverseExpression)
    @Override String getInverseExpression();
    @Override TermMapRml1 setInverseExpression(String inverseExpression);

    /**
     *
     * https://www.w3.org/TR/r2rml/#dfn-column-valued-term-map
     *
     */
    @Override
    default boolean isColumnValued() {
        return hasProperty(RR.column);
    }

    /**
     * Predicate to test whether this term map qualifies according to
     *
     * https://www.w3.org/TR/r2rml/#dfn-template-valued-term-map
     *
     * @return
     */
    @Override
    default boolean isTemplateValued() {
        return hasProperty(RR.template);
    }
}
