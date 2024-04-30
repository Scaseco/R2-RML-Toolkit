package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface SubjectMap
    extends TermMap, HasGraphMap
{

    /**
     * Return a set view (never null) of resources specified via rr:class
     *
     * @return
     */
    @Iri(Rml2Terms.xclass)
    Set<Resource> getClasses();
}
