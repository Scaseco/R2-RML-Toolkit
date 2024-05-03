package org.aksw.rmltk.model.backbone.rml;

import org.aksw.rmltk.model.backbone.common.IAbstractSource;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.apache.jena.rdf.model.Resource;

/**
 * Rml's extended attribute of rr:TriplesMap
 */
public interface ITriplesMapRml
    extends ITriplesMap
{
    ILogicalSource getLogicalSource();
    ITriplesMapRml setLogicalSource(Resource logicalSource);

//    @Override
//    default SubjectMap getOrSetSubjectMap() {
//        SubjectMap result = getSubjectMap();
//
//        if (result == null) {
//            result = getModel().createResource().as(SubjectMap.class);
//            setSubjectMap(result);
//        }
//
//        return result;
//    }
//
    @Override
    default IAbstractSource getAbstractSource() {
        return getLogicalSource();
    }

    @Override
    default ITriplesMapRml setAbstractSource(IAbstractSource abstractSource) {
        return setLogicalSource(abstractSource);
    }
}
