package org.aksw.rml.jena.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.r2rml.jena.arq.impl.JoinDeclaration;
import org.aksw.r2rml.jena.arq.impl.MappingCxt;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.jena.plugin.ReferenceFormulationService;
import org.aksw.rml.model.LogicalSourceRml1;
import org.aksw.rml.model.TriplesMapRml1;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
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
    /** For every non-joining object map create an individual query */
    public static List<Query> createCanonicalQueries(TriplesMapToSparqlMapping mapping, boolean pushDistinct, ReferenceFormulationService registry) {
        if (registry == null) {
            registry = ReferenceFormulationRegistry.get();
        }

        LogicalSourceRml1 childSource = mapping.getTriplesMap().as(TriplesMapRml1.class).getLogicalSource();
        String childRfIri = childSource.getReferenceFormulationIri();
        ReferenceFormulation childRf = registry.getOrThrow(childRfIri);
        Element childElt = childRf.source(childSource, mapping.getMappingCxt().getTriplesMapVar());
        // ElementBind childSubjectElt = join.getChildSubjectDefinition();

        List<Quad> quads = mapping.getTemplate().getQuads();
        List<Query> result = new ArrayList<>(quads.size());
        for (Quad quad : quads) {
            ElementGroup elt = new ElementGroup();

            List<Expr> roots = QuadUtils.streamNodes(quad).map(ExprLib::nodeToExpr).collect(Collectors.toList());

            GenericDag<Expr, Var> baseExprDag = mapping.getExprDag();
            GenericDag<Expr, Var> exprDag;

            // XXX Push distinct is not optimal for XML because the core definitions typically lead to XML fragments
            List<Expr> coreDefs;

            // Push distinct sets up a DISTINCT sub-query in such a way that it may be a common expression to all mappings that make use of the same set of core expressions
            // remaps variables of core definitions, such as ?v1 := json:path(?s0, "$.foo"), to variable names derived from the expression string
            // So ?v1 becomes something like ?json_path_s0_foo.
            if (pushDistinct) {
                Query distinctQuery = new Query();
                distinctQuery.setQuerySelectType();
                distinctQuery.setDistinct(true);
                ElementGroup coreGroup = new ElementGroup();
                coreGroup.addElement(childElt);

                GenericDag<Expr, Var> subDag = baseExprDag.subDag(roots);

                coreDefs = new ArrayList<>(GenericDag.getCoreDefinitions(subDag, roots));

                // Set up a remapping of the variable names
                Map<Var, Var> remap = new HashMap<>();
                for (Expr coreDef : coreDefs) {
                    Var coreVar = subDag.getVar(coreDef);
                    String baseName = subDag.getExprOps().toString(coreDef);
                    Var safeVar = VarUtils.safeVar(baseName);
                    remap.put(coreVar, safeVar);
                }
                exprDag = subDag.applyVarTransform(remap::get);

                // Sort core definitions so they have the same order across multiple queries
                Collections.sort(coreDefs, (x, y) -> exprDag.getExprOps().toString(x).compareTo(exprDag.getExprOps().toString(y)));

                for (Expr coreDef : coreDefs) {
                    Var coreVar = exprDag.getVar(coreDef);
                    // if ( x != null) {
                        coreGroup.addElement(new ElementBind(coreVar, coreDef));
                    //}
                    distinctQuery.getProject().add(coreVar);
                }

                distinctQuery.setQueryPattern(coreGroup);
                elt.addElement(new ElementSubQuery(distinctQuery));

                exprDag.getVarToExpr().keySet().removeAll(remap.values());
                exprDag.collapse();
            } else {
                elt.addElement(childElt);
                exprDag = baseExprDag.subDag(roots);
                exprDag.collapse();
                coreDefs = List.of();
            }

            Map<Var, Expr> defs = GenericDag.getSortedDependencies(exprDag, roots);
            for (Entry<Var, Expr> e : defs.entrySet()) { // join.getChildCxt().getExprDag().getVarToExpr().entrySet()) {
                Expr x = e.getValue();
                if (coreDefs.contains(x)) {
                    continue;
                }

                if ( x != null) {
                    elt.addElement(new ElementBind(e.getKey(), x));
                }
            }

            Query query = new Query();
            query.setQueryConstructType();
            query.setConstructTemplate(new Template(new QuadAcc(List.of(quad))));
            // query.setDistinct(preDistinct);
            query.setQueryPattern(elt);

            result.add(query);
        }

//        VarExprList varToExpr = mapping.getExpandedVarExprList();
//        varToExpr.forEachVarExpr((v, e) ->  {
////            Expr ee = !safeVars
////                    ? e
////                    : ExprTransformer.transform(new NodeTransformExpr(n -> n.isVariable() ? VarUtils.safeVar(n.getName()) : n), e);
//            elt.addElement(new ElementBind(v, e));
//        });


        return result;

    }

    /** Produces a query from all non-joining elements */
    public static Query createQuery(TriplesMapToSparqlMapping mapping, ReferenceFormulationService registry) {
        if (registry == null) {
            registry = ReferenceFormulationRegistry.get();
        }

        LogicalSourceRml1 childSource = mapping.getTriplesMap().as(TriplesMapRml1.class).getLogicalSource();
        String childRfIri = childSource.getReferenceFormulationIri();
        ReferenceFormulation childRf = registry.getOrThrow(childRfIri);
        Element childElt = childRf.source(childSource, mapping.getMappingCxt().getTriplesMapVar());
        // ElementBind childSubjectElt = join.getChildSubjectDefinition();

        ElementGroup elt = new ElementGroup();
        elt.addElement(childElt);

//        VarExprList varToExpr = mapping.getExpandedVarExprList();
//        varToExpr.forEachVarExpr((v, e) ->  {
////            Expr ee = !safeVars
////                    ? e
////                    : ExprTransformer.transform(new NodeTransformExpr(n -> n.isVariable() ? VarUtils.safeVar(n.getName()) : n), e);
//            elt.addElement(new ElementBind(v, e));
//        });
        Map<Var, Expr> defs = GenericDag.getSortedDependencies(mapping.getExprDag());
        for (Entry<Var, Expr> e : defs.entrySet()) { // join.getChildCxt().getExprDag().getVarToExpr().entrySet()) {
            Expr x = e.getValue();
            if ( x != null) {
                elt.addElement(new ElementBind(e.getKey(), x));
            }
        }


        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(mapping.getTemplate());
        result.setQueryPattern(elt);

        return result;
    }

    /**
     *
     * @param join
     * @param preDistinct Perform a distinct operation on both sides of the join
     * @param registry
     * @return
     */
    public static Query createQuery(JoinDeclaration join, boolean preDistinct, ReferenceFormulationService registry) {
        if (registry == null) {
            registry = ReferenceFormulationRegistry.get();
        }

        Element childGrp = createJoinGroup(registry, join, preDistinct, join.getChildCxt(), E_Equals::getArg2);
        Element parentGrp = createJoinGroup(registry, join, preDistinct, join.getParentCxt(), E_Equals::getArg1);

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

    public static Element createJoinGroup(ReferenceFormulationService registry, JoinDeclaration join, boolean preDistinct, MappingCxt cxt, Function<E_Equals, Expr> getConditions) {
        LogicalSourceRml1 source = cxt.getTriplesMap().as(TriplesMapRml1.class).getLogicalSource();
        String rfIri = source.getReferenceFormulationIri();
        ReferenceFormulation rf = registry.getOrThrow(rfIri);
        Element sourceElt = rf.source(source, cxt.getTriplesMapVar());
        Var subjectVar = cxt.getSubjectVar();
        ExprVar subjectEv = new ExprVar(cxt.getSubjectVar());

        ElementGroup pattern = new ElementGroup();
        pattern.addElement(sourceElt);

        // TODO Collapse the expressions for the subject and the join condition to micro-optimize further
        // TODO Right now we assume that the join condition is expended (no lookup in the dag needed)
        //  Right now we emit all intermediate expressions

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

        GenericDag<Expr, Var> exprDag = cxt.getExprDag().cloneObject();
        exprDag.collapse();

        Map<Var, Expr> defs = GenericDag.getSortedDependencies(exprDag, Arrays.asList(subjectEv));
        for (Entry<Var, Expr> e : defs.entrySet()) { // join.getChildCxt().getExprDag().getVarToExpr().entrySet()) {
            Expr x = e.getValue();
            if ( x != null) {
                pattern.addElement(new ElementBind(e.getKey(), x));
            }
        }

        Query query = new Query();
        query.setQuerySelectType();
        query.addProjectVars(joinVars);
        query.getProject().add(subjectVar);
        query.setQueryPattern(pattern);

        if (preDistinct) {
            query.setDistinct(true);
        }

        return new ElementSubQuery(query);
    }
}
