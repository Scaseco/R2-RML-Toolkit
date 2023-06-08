package org.aksw.rmlx.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.model.prefix.domain.api.NorsePrefixTerms;
import org.aksw.jenax.model.prefix.domain.api.PrefixSet;
import org.aksw.rml.jena.impl.NorseRmlTerms;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RmlDefinitionBlock
    extends Resource
{
    @Iri(NorsePrefixTerms.prefixes)
    Set<PrefixSet> getPrefixSets();

    @Iri(NorseRmlTerms.alias)
    Set<RmlAlias> getAliases();

    @Iri(NorseRmlTerms.bind)
    Set<RmlBind> getBinds();
}
