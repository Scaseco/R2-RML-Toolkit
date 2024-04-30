package org.aksw.rml.jena.common;


import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface R2rmlView
    extends LogicalTable
{
    String getSqlQuery();
    R2rmlView setSqlQuery(String queryString);

    Set<Resource> getSqlVersions();

    /**
     * Convenience view of the resource IRIs as strings
     *
     * @return
     */
    Set<String> getSqlVersionIris();
}
