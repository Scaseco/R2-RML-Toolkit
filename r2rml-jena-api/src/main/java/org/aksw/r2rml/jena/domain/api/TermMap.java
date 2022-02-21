package org.aksw.r2rml.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.r2rml.jena.vocab.RR;
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
	extends MappingComponent
{
	@Iri(R2rmlTerms.termType)
	Resource getTermType();
	TermMap setTermType(Resource termType);

	@Iri(R2rmlTerms.column)
	String getColumn();
	TermMap setColumn(String columnName);
	
	@Iri(R2rmlTerms.language)
	String getLanguage();
	TermMap setLanguage(String language);
	
	@Iri(R2rmlTerms.datatype)
	Resource getDatatype();
	TermMap setDatatype(Resource datatype);
	
	@Iri(R2rmlTerms.constant)
	RDFNode getConstant();
	TermMap setConstant(RDFNode constant);

	@Iri(R2rmlTerms.template)
	String getTemplate();
	TermMap setTemplate(String template);

	@Iri(R2rmlTerms.inverseExpression)
	String getInverseExpression();
	TermMap setInverseExpression(String inverseExpression);

	/**
	 * 
	 * https://www.w3.org/TR/r2rml/#dfn-column-valued-term-map
	 * 
	 */
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
	default boolean isTemplateValued() {
		return hasProperty(RR.template);
	}

}
