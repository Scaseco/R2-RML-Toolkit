package org.aksw.r2rmlx.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.jena.domain.api.MappingComponent;
import org.aksw.r2rmlx.vocab.RRX;

@ResourceView
public interface Constraint
	extends MappingComponent
{
	default boolean qualifiesAsPrefixConstraint() {
		return hasProperty(RRX.prefix);
	}

	default PrefixConstraint asPrefixConstraint() {
		return as(PrefixConstraint.class);
	}
	
	
	default boolean qualifiesAsRangeConstraint() {
		return hasProperty(RRX.min) || hasProperty(RRX.max);
	}	

	default RangeConstraint asRangeConstraint() {
		return as(RangeConstraint.class);
	}
}
