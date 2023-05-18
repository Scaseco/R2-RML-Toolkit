package org.aksw.rmlx.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.rml.jena.impl.NorseRmlTerms;
import org.aksw.rml.model.LogicalSource;

public interface LogicalSourceWithAliases
    extends LogicalSource
{
    @Iri(NorseRmlTerms.alias)
    Set<SparqlAlias> getAliases();
}
