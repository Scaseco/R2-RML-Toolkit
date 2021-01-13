package org.aksw.r2rmlx.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;

public interface TermMapX
	extends TermMap
{
	@Iri(R2RMLXStrings.languageColumn)
	String getLangColumn();
	TermMapX setLangColumn(String langColumn);
	
	@Iri(R2RMLXStrings.constraint)
	Set<Constraint> getConstraints();
	
	
	default Constraint addNewConstraint() {
		Constraint result = getModel().createResource().as(Constraint.class);
		getConstraints().add(result);
		return result;
	}
}
