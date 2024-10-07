package org.aksw.rml.v2.io;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.RmlIoTerms;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RelativePathSource
    extends Resource
{
    @Iri(RmlIoTerms.root)
    RDFNode getRoot();
    RelativePathSource setRoot(RDFNode root);

    @Iri(RmlIoTerms.path)
    String getPath();
    RelativePathSource setPath(String path);

    default boolean qualifiesAsRelativePathSource() {
        boolean result = false;
        RDFNode root = getRoot();
        if (root != null) {
            result = true;
        } else {
            String path = getPath();
            if (path != null) {
                result = true;
            }
        }
        return result;
    }
}
