package org.aksw.rmltk.model.r2rml;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.ISubjectMap;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface SubjectMap
    extends ISubjectMap, TermMap, HasGraphMap
{

    /**
     * Return a set view (never null) of resources specified via rr:class
     *
     * @return
     */
    @Iri(R2rmlTerms.xclass)
    @Override Set<Resource> getClasses();
}
