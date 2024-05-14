package org.aksw.rmltk.model.r2rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.r2rml.jena.vocab.RR;
import org.aksw.rmltk.model.backbone.common.IDatatypeMap;
import org.aksw.rmltk.model.backbone.common.ITermMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A TermMap with all attributes according to the R2RML specification.
 *
 * @author Claus Stadler
 *
 */
@ResourceView
public interface TermMap
    extends ITermMap, TermSpec
{
    @Iri(R2rmlTerms.termType)
    @Override Resource getTermType();
    @Override TermMap setTermType(Resource termType);

    @Iri(R2rmlTerms.column)
    @Override String getColumn();
    @Override TermMap setColumn(String columnName);

    @Iri(R2rmlTerms.language)
    @Override String getLanguage();
    @Override TermMap setLanguage(String language);

    @Iri(R2rmlTerms.datatype)
    @Override Resource getDatatype();
    @Override TermMap setDatatype(Resource datatype);

    @Override
    default IDatatypeMap getDatatypeMap() {
        return null;
    }

    @Override
    default TermMap setDatatypeMap(IDatatypeMap datatypeMap) {
        if (datatypeMap != null) {
            throw new UnsupportedOperationException("DatatypeMap is not supported in this model");
        }
        return this;
    }

    @Iri(R2rmlTerms.constant)
    @Override RDFNode getConstant();
    @Override TermMap setConstant(RDFNode constant);

    @Iri(R2rmlTerms.template)
    @Override String getTemplate();
    @Override TermMap setTemplate(String template);

    @Iri(R2rmlTerms.inverseExpression)
    @Override String getInverseExpression();
    @Override TermMap setInverseExpression(String inverseExpression);

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
