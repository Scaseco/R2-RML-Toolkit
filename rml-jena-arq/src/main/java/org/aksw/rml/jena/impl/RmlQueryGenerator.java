package org.aksw.rml.jena.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.r2rml.jena.arq.impl.JoinDeclaration;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.model.LogicalSource;
import org.aksw.rml.model.RmlTriplesMap;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;

public class RmlQueryGenerator {
    // protected ReferenceFormulationRegistry registry;

    public static Query createQuery(JoinDeclaration join, ReferenceFormulationRegistry registry) {
        if (registry == null) {
            registry = ReferenceFormulationRegistry.get();
        }

        LogicalSource childSource = join.getChildTriplesMap().as(RmlTriplesMap.class).getLogicalSource();
        LogicalSource parentSource = join.getParentTriplesMap().as(RmlTriplesMap.class).getLogicalSource();

        String childRfIri = childSource.getReferenceFormulationIri();
        ReferenceFormulation childRf = registry.getOrThrow(childRfIri);

        String parentRfIri = parentSource.getReferenceFormulationIri();
        ReferenceFormulation parentRf = registry.getOrThrow(parentRfIri);

        Element childElt = childRf.source(childSource, join.getChildVar());
        Element parentElt = parentRf.source(parentSource, join.getParentVar());

        List<Element> elts = new ArrayList<>();
        elts.add(childElt);
        // TODO We need to add subject map expressions
        elts.add(parentElt);

        for (Expr expr : join.getConditionExprs()) {
            elts.add(new ElementFilter(expr));
        }

        Element elt = ElementUtils.groupIfNeeded(elts);

        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(new Template(new QuadAcc(Arrays.asList(join.getQuad()))));
        result.setQueryResultStar(true);
        result.setQueryPattern(elt);
        return result;
    }
}
