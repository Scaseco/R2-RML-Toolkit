package org.aksw.rmltk.model.backbone.r2rml;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface IR2rmlView
    extends ILogicalTableR2rml
{
    String getSqlQuery();
    IR2rmlView setSqlQuery(String queryString);

    Set<Resource> getSqlVersions();

    /**
     * Convenience view of the resource IRIs as strings
     *
     * @return
     */
    Set<String> getSqlVersionIris();
}
