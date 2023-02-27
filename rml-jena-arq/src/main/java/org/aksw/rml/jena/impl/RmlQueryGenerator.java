package org.aksw.rml.jena.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.r2rml.jena.arq.impl.JoinDeclaration;
import org.aksw.r2rml.jena.arq.impl.MappingCxt;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.model.LogicalSource;
import org.aksw.rml.model.RmlTriplesMap;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.Template;

public class RmlQueryGenerator {
    /** Produces a query from all non-joining elements */
    public static Query createQuery(TriplesMapToSparqlMapping mapping, ReferenceFormulationRegistry registry) {
        if (registry == null) {
            registry = ReferenceFormulationRegistry.get();
        }

        LogicalSource childSource = mapping.getTriplesMap().as(RmlTriplesMap.class).getLogicalSource();
        String childRfIri = childSource.getReferenceFormulationIri();
        ReferenceFormulation childRf = registry.getOrThrow(childRfIri);
        Element childElt = childRf.source(childSource, mapping.getMappingCxt().getTriplesMapVar());
        // ElementBind childSubjectElt = join.getChildSubjectDefinition();

        ElementGroup elt = new ElementGroup();
        elt.addElement(childElt);

        VarExprList varToExpr = mapping.getExpandedVarExprList();
        varToExpr.forEachVarExpr((v, e) ->  {
//            Expr ee = !safeVars
//                    ? e
//                    : ExprTransformer.transform(new NodeTransformExpr(n -> n.isVariable() ? VarUtils.safeVar(n.getName()) : n), e);
            elt.addElement(new ElementBind(v, e));
        });


        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(mapping.getTemplate());
        result.setQueryPattern(elt);

        return result;
    }

    public static Query createQuery(JoinDeclaration join, ReferenceFormulationRegistry registry) {
        if (registry == null) {
            registry = ReferenceFormulationRegistry.get();
        }
//
//        LogicalSource childSource = join.getChildTriplesMap().as(RmlTriplesMap.class).getLogicalSource();
//        LogicalSource parentSource = join.getParentTriplesMap().as(RmlTriplesMap.class).getLogicalSource();
//
//        String childRfIri = childSource.getReferenceFormulationIri();
//        ReferenceFormulation childRf = registry.getOrThrow(childRfIri);
//
//        String parentRfIri = parentSource.getReferenceFormulationIri();
//        ReferenceFormulation parentRf = registry.getOrThrow(parentRfIri);
//
//        Element childSourceElt = childRf.source(childSource, join.getChildVar());
//        Element parentSourceElt = parentRf.source(parentSource, join.getParentVar());
//
//        join.getChildSubjectDefinition()
////        ElementBind childSubjectElt = join.getChildSubjectDefinition();
////        ElementBind parentSubjectElt = join.getParentSubjectDefinition();
//
//        ElementGroup elt = new ElementGroup();

        // Build the syntax such that it enforces a hash join:
        // SELECT * WHERE {
        //   { SERVICE <childSource> { } BIND(childJoinConditionExpr As ?jc) }
        //   { SERVICE <parentSource> { } BIND(parentJoinConditionExpr As ?jc) }
        // }

        // Child group
//        ElementGroup childGrp = new ElementGroup();
//        childGrp.addElement(childSourceElt);
//        Map<Var, Expr> childDefs = GenericDag.getSortedDependencies(join.getChildCxt().getExprDag());
//        for (Entry<Var, Expr> e : childDefs.entrySet()) { // join.getChildCxt().getExprDag().getVarToExpr().entrySet()) {
//            Expr x = e.getValue();
//            if ( x != null) {
//                childGrp.addElement(new ElementBind(e.getKey(), x));
//            }
//        }
//        for (int i = 0; i < joinExprs.size(); ++i) {
//            Expr expr = joinExprs.get(i);
//            E_Equals eq = (E_Equals)expr;
//            Var jcVar = Var.alloc("jc" + i);
//            childGrp.addElement(new ElementBind(jcVar, eq.getArg2()));
//            // It is important to filter out non-bound variables - sparql's natural hash join would otherwise result in an outer join but we want an inner one
//            childGrp.addElementFilter(new ElementFilter(new E_Bound(new ExprVar(jcVar))));
//        }

        Element childGrp = createJoinGroup(registry, join, join.getChildCxt(), E_Equals::getArg2);
        Element parentGrp = createJoinGroup(registry, join, join.getParentCxt(), E_Equals::getArg1);

        // elt.addElement(childGrp);
        // elt.addElement(parentGrp);

//        for (Expr expr : join.getConditionExprs()) {
//            elt.addElementFilter(new ElementFilter(expr));
//        }

        ElementGroup elt = new ElementGroup();
        elt.addElement(childGrp);
        elt.addElement(parentGrp);

        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(new Template(new QuadAcc(Arrays.asList(join.getQuad()))));
        result.setQueryResultStar(true);
        result.setQueryPattern(elt);
        return result;
    }


    public static Element createJoinGroup(ReferenceFormulationRegistry registry, JoinDeclaration join, MappingCxt cxt, Function<E_Equals, Expr> getConditions) {
        LogicalSource source = cxt.getTriplesMap().as(RmlTriplesMap.class).getLogicalSource();
        String rfIri = source.getReferenceFormulationIri();
        ReferenceFormulation rf = registry.getOrThrow(rfIri);
        Element sourceElt = rf.source(source, join.getChildVar());
        ExprVar subjectRoot = new ExprVar(cxt.getSubjectVar());

        // Parent group
        ElementGroup pattern = new ElementGroup();
        pattern.addElement(sourceElt);
//        for (Entry<Var, Expr> e : join.getParentCxt().getExprDag().getVarToExpr().entrySet()) {
//            parentGrp.addElement(new ElementBind(e.getKey(), e.getValue()));
//        }
        Map<Var, Expr> defs = GenericDag.getSortedDependencies(cxt.getExprDag(), Arrays.asList(subjectRoot));
        for (Entry<Var, Expr> e : defs.entrySet()) { // join.getChildCxt().getExprDag().getVarToExpr().entrySet()) {
            Expr x = e.getValue();
            if ( x != null) {
                pattern.addElement(new ElementBind(e.getKey(), x));
            }
        }
        ExprList joinExprs = join.getConditionExprs();
        List<Var> joinVars = new ArrayList<>(joinExprs.size());
        for (int i = 0; i < joinExprs.size(); ++i) {
            Expr expr = joinExprs.get(i);
            E_Equals eq = (E_Equals)expr;
            Var jcVar = Var.alloc("jc" + i);
            Expr cond = getConditions.apply(eq);
            pattern.addElement(new ElementBind(jcVar, cond));
            pattern.addElementFilter(new ElementFilter(new E_Bound(new ExprVar(jcVar))));

            joinVars.add(jcVar);
        }

        Query query = new Query();
        query.setQuerySelectType();
        query.addProjectVars(joinVars);
        query.addProjectVars(defs.keySet());
        query.setQueryPattern(pattern);

        return new ElementSubQuery(query);
    }
}
