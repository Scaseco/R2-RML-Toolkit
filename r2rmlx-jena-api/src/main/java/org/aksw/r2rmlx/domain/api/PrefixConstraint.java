package org.aksw.r2rmlx.domain.api;

import java.util.Arrays;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
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
