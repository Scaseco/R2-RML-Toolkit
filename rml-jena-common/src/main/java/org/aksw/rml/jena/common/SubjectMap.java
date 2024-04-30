package org.aksw.rml.jena.common;


import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface SubjectMap
    extends TermMap, HasGraphMap
{

    /**
     * Return a set view (never null) of resources specified via rr:class
     *
     * @return
     */
    Set<Resource> getClasses();
}
