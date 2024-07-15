package org.aksw.rmltk.model.r2rml;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.r2rml.IR2rmlView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface R2rmlView
    extends IR2rmlView, LogicalTable
{
    @Iri(R2rmlTerms.sqlQuery)
    @Override String getSqlQuery();
    @Override R2rmlView setSqlQuery(String queryString);

    @Iri(R2rmlTerms.sqlVersion)
    @Override Set<Resource> getSqlVersions();

    /**
     * Convenience view of the resource IRIs as strings
     *
     * @return
     */
    @Iri(R2rmlTerms.sqlVersion)
    @IriType
    @Override Set<String> getSqlVersionIris();
}
