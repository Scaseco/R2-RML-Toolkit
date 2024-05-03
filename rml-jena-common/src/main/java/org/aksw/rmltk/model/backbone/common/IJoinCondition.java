package org.aksw.rmltk.model.backbone.common;

import org.apache.jena.rdf.model.Resource;

public interface IJoinCondition
    extends Resource
{
    String getParent();
    IJoinCondition setParent(String parent);

    String getChild();
    IJoinCondition setChild(String child);
}
