package org.aksw.rmltk.model.backbone.common;


import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface ISubjectMap
    extends ITermMap, IHasGraphMap
{

    /**
     * Return a set view (never null) of resources specified via rr:class
     *
     * @return
     */
    Set<Resource> getClasses();
}
