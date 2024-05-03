package org.aksw.rmltk.model.backbone.common;


import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A TermMap with all attributes according to the R2RML specification.
 *
 * @author Claus Stadler
 *
 */
public interface ITermMap
    extends ITermSpec
{
    Resource getTermType();
    ITermMap setTermType(Resource termType);

    String getColumn();
    ITermMap setColumn(String columnName);

    String getLanguage();
    ITermMap setLanguage(String language);

    Resource getDatatype();
    ITermMap setDatatype(Resource datatype);

    RDFNode getConstant();
    ITermMap setConstant(RDFNode constant);

    String getTemplate();
    ITermMap setTemplate(String template);

    String getInverseExpression();
    ITermMap setInverseExpression(String inverseExpression);

    /**
     *
     * https://www.w3.org/TR/r2rml/#dfn-column-valued-term-map
     *
     */
    boolean isColumnValued();

    /**
     * Predicate to test whether this term map qualifies according to
     *
     * https://www.w3.org/TR/r2rml/#dfn-template-valued-term-map
     *
     * @return
     */
    boolean isTemplateValued();
}
