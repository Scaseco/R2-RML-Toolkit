package org.aksw.rmltk.model.backbone.rml;


import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface ISubjectMapRml
    extends ITermMapRml, IHasGraphMapRml
{

    /**
     * Return a set view (never null) of resources specified via rr:class
     *
     * @return
     */
    Set<Resource> getClasses();
}
