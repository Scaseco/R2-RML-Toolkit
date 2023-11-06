package org.aksw.rmlx.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.model.shacl.domain.ShHasPrefixes;
import org.aksw.rml.jena.impl.NorseRmlTerms;

@ResourceView
public interface RmlDefinitionBlock
    extends ShHasPrefixes
{
    @Iri(NorseRmlTerms.alias)
    Set<RmlAlias> getAliases();

    @Iri(NorseRmlTerms.bind)
    Set<String> getBinds();

    /** A more verbose variant of bind */
    @Iri(NorseRmlTerms.qualifiedBind)
    Set<RmlQualifiedBind> getQualifiedBinds();

    /**
     * IRI of a0 multi-valued property for SPARQL filter expressions.
     * Filters can be part of LogicalSources, TripleMaps and TermMaps.
     * By convention, the filter expressions on term maps can make use of the variable '?this' to refer to the term maps value.
     * Filters making use of '?this' are thus by default evaluated after a term map's resulting term has been constructed.
     * Optimizers may transform filter conditions.
     *
     * The expression can make use of any alias (or column in the case of tabular data).
     * A term map can introduce 'local' aliases for use in filters.
     */
    @Iri(NorseRmlTerms.filter)
    Set<String> getFilters();
}
