package org.aksw.rml.jena.impl;

import java.nio.file.Path;
import java.util.Collection;

import org.aksw.commons.util.obj.HasSelf;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.riot.Lang;

public interface RmlModelImporterMixin<X extends RmlModelImporterMixin<X>> extends HasSelf<X> {

    RmlModelImporter getRmlModelImporter();

    /** Process a turtle string. */
    default X addRmlString(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String str) {
        getRmlModelImporter().addRmlString(rmlTriplesMapClass, str);
        return self();
    }

    default X addRmlString(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String str, Lang lang) {
        getRmlModelImporter().addRmlString(rmlTriplesMapClass, str, lang);
        return self();
    }

    default X addRmlFiles(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Collection<String> rmlFiles) {
        getRmlModelImporter().addRmlFiles(rmlTriplesMapClass, rmlFiles);
        return self();
    }

    default X addRmlPaths(Class<? extends ITriplesMapRml> rmlTriplesMapClass, Collection<Path> rmlFiles) {
        getRmlModelImporter().addRmlPaths(rmlTriplesMapClass, rmlFiles);
        return self();
    }

    default X addRmlFile(Class<? extends ITriplesMapRml> rmlTriplesMapClass, String rmlFile) {
        getRmlModelImporter().addRmlFile(rmlTriplesMapClass, rmlFile);
        return self();
    }
}
