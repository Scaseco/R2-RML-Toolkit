package org.aksw.rmlx.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.model.shacl.domain.ShHasPrefixes;
import org.aksw.rml.jena.impl.NorseRmlTerms;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RmlDefinitionBlock
    extends Resource, ShHasPrefixes
{
//    @Iri(NorsePrefixTerms.prefixes)
//    Set<PrefixSet> getPrefixSets();

    @Iri(NorseRmlTerms.alias)
    Set<RmlAlias> getAliases();

    @Iri(NorseRmlTerms.bind)
    Set<String> getBinds();

    /** A more verbose variant of bind */
    @Iri(NorseRmlTerms.qualifiedBind)
    Set<RmlQualifiedBind> getQualifiedBinds();
}
