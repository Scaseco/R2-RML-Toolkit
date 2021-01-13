package org.aksw.r2rmlx.domain.api;

import java.util.Arrays;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;

@ResourceView
public interface PrefixConstraint
	extends Constraint
{
	@Iri(R2RMLXStrings.prefix)
	Set<String> getPrefixes();
	
	
	default PrefixConstraint addPrefixes(String ...prefixes) {
		getPrefixes().addAll(Arrays.asList(prefixes));
		return this;
	}
}
