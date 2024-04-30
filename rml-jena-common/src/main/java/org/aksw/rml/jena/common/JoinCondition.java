package org.aksw.rml.jena.common;


import org.apache.jena.rdf.model.Resource;

public interface JoinCondition
    extends Resource
{
    String getParent();
    JoinCondition setParent(String parent);

    String getChild();
    JoinCondition setChild(String child);
}
