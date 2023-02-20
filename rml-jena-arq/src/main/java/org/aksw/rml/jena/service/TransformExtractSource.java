package org.aksw.rml.jena.service;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpService;

public class TransformExtractSource
    extends TransformCopy
{
    @Override
    public Op transform(OpService opService, Op subOp) {
        Node rmlSource = NodeFactory.createURI("rml.source:");
        Op result;
        if (rmlSource.equals(opService.getService())) {
            result = OpUtils.createEmptyTableUnionVars(opService.getSubOp());
        } else {
            result = super.transform(opService, subOp);
        }
        return result;
    }
}
