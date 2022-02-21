package org.aksw.r2rml.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface JoinCondition 
	extends Resource
{
	@Iri(R2rmlTerms.parent)
	String getParent();
	JoinCondition setParent(String parent);
	
	@Iri(R2rmlTerms.child)
	String getChild();
	JoinCondition setChild(String child);
}
