package org.aksw.fnox.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@Namespace("http://example.com/library#")
public interface JavaMethodReference
    extends Resource
{
    /** Path to a jar file that contains the function */
    @IriNs
    String getLocalLibrary();
    JavaMethodReference setLocalLibrary(String localLibrary);

    @Iri(":class")
    String getClassName();
    JavaMethodReference setClassName(String className);

    @IriNs
    String getMethod();
    JavaMethodReference setMethod(String method);


    default String toUri() {
        return "java:" + getClassName() + "#" + getMethod() + "@" + getLocalLibrary();
    }
}
