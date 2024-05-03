package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.ISubjectMap;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface SubjectMapRml2
    extends ISubjectMap, TermMapRml2, HasGraphMapRml2
{
    /**
     * Return a set view (never null) of resources specified via rr:class
     *
     * @return
     */
    @Iri(Rml2Terms.xclass)
    @Override
    Set<Resource> getClasses();
}
