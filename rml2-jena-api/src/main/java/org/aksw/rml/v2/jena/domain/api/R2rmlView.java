package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface R2rmlView
    extends LogicalTable
{
    @Iri(Rml2Terms.sqlQuery)
    String getSqlQuery();
    R2rmlView setSqlQuery(String queryString);

    @Iri(Rml2Terms.sqlVersion)
    Set<Resource> getSqlVersions();

    /**
     * Convenience view of the resource IRIs as strings
     *
     * @return
     */
    @Iri(Rml2Terms.sqlVersion)
    @IriType
    Set<String> getSqlVersionIris();
}
