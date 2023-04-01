package org.aksw.rml.jena.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.fno.model.FnoTerms;
import org.aksw.fno.model.Param;
import org.aksw.fnox.model.JavaFunction;
import org.aksw.fnox.model.JavaMethodReference;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.triple.GraphVarImpl;
import org.aksw.jenax.arq.util.update.UpdateUtils;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.ObjectMapType;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.TermSpec;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.rml.jena.impl.Clusters.Cluster;
import org.aksw.rml.model.LogicalSource;
import org.aksw.rml.model.Rml;
import org.apache.hadoop.thirdparty.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;


public class RmlLib {
    public static void normalizeNamespace(Model model) {
        UpdateUtils.renameNamespace(model.getGraph(), FnoTerms.OLD_NS, FnoTerms.NS);
    }

    @Deprecated // We now have a proper RML model so we don't need to rely on such hacks
    public static void renameRmlToR2rml(Model model) {
        UpdateUtils.renameProperty(model.getGraph(), "http://semweb.mmlab.be/ns/rml#reference", "http://www.w3.org/ns/r2rml#column");
        UpdateUtils.renameProperty(model.getGraph(), "http://semweb.mmlab.be/ns/rml#source", "http://www.w3.org/ns/r2rml#tableName");
        UpdateUtils.renameProperty(model.getGraph(), "http://semweb.mmlab.be/ns/rml#logicalSource", "http://www.w3.org/ns/r2rml#logicalTable");
    }


    /**
     * Extract a logical source from a service node.
     * Attempts to collect the SPARQL expression's triples into an RDF graph.
     */
    public static LogicalSource getLogicalSource(OpService opService) {
        Op subOp = opService.getSubOp();
        Query query = OpAsQuery.asQuery(subOp);
        Element elt = query.getQueryPattern();
        Graph graph = ElementUtils.toGraph(elt, new GraphVarImpl());
        Model model = ModelFactory.createModelForGraph(graph);
        LogicalSource result = RmlLib.getOnlyLogicalSource(model);
        return result;
    }

    /** Extract the only logical source from a given model. Null if none found; exception if more than one. */
    public static LogicalSource getOnlyLogicalSource(Model model) {
        List<LogicalSource> matches = model.listResourcesWithProperty(Rml.source)
                .mapWith(r -> r.as(LogicalSource.class))
                .toList();
        LogicalSource result = IterableUtils.expectZeroOrOneItems(matches);
        return result;
    }

    public static Expr buildFunctionCall(Model fnmlModel, TriplesMap rawFnMap) {
        Model extra = ModelFactory.createDefaultModel();
        Model union = ModelFactory.createUnion(rawFnMap.getModel(), extra);

        TriplesMap fnMap = rawFnMap.inModel(union).as(TriplesMap.class);
        // Add a dummy subject in order to allow for passing it throw the standard R2RML machinery
        fnMap.setSubjectIri("urn:x-r2rml:dummy-subject");

        TriplesMapToSparqlMapping mapping = RmlImporterLib.read(fnMap, fnmlModel);
        Map<TermSpec, Var> tmToVar = mapping.getTermMapToVar();
        VarExprList varToExpr = mapping.getVarToExpr(); //getVarToExpr();

        Map<String, ObjectMapType> args = new HashMap<>();
        for (PredicateObjectMap pom : fnMap.getPredicateObjectMaps()) {
            String p = pom.getPredicateIri();
            if (p == null) {
                p = pom.getPredicateMap().getConstant().asNode().getURI();
            }

            // TODO Check for re-assignment
            args.put(p, pom.getObjectMap());
        }

        ObjectMap om = args.get(FnoTerms.executes).asTermMap();
        Node fnId = om.asTermMap().getConstant().asNode();
        RDFNode fnn = fnmlModel.asRDFNode(fnId);
        if (fnn == null) {
            throw new RuntimeException("No function declaration found for" + fnId);
        }

        ExprList el = new ExprList();
        JavaFunction fn = fnn.as(JavaFunction.class);
        JavaMethodReference ref = fn.getProvidedBy();
        String javaFunctionIri = ref.toUri();

        for (Param param : fn.getExpects()) {
            String p = param.getPredicateIri();
            ObjectMapType omt = args.get(p);
            Var var = tmToVar.get(omt);
            Expr expr = varToExpr.getExpr(var);
            el.add(expr);
        }

        Expr result = new E_Function(javaFunctionIri, el);
        return result;
    }


    /** Count the number of RML service blocks in a query */
    public static class RmlServiceGroupCounter
        extends ElementVisitorBase {
        protected int counter = 0;

        public int getCounter() {
            return counter;
        }

        @Override
        public void visit(ElementSubQuery el) {
            ElementWalker.walk(el.getQuery().getQueryPattern(), this);
        }
//        @Override
//        public void visit(ElementGroup el) {
//            if (isRmlServiceGroup(el)) {
//                ++counter;
//            }
//            super.visit(el);
//        }

        @Override
        public void visit(ElementService el) {
            if (isRmlServiceNode(el.getServiceNode())) {
                ++counter;
            } else if (NodeFactory.createURI("cache:").equals(el.getServiceNode())) {
                ElementWalker.walk(el.getElement(), this);
            }
            super.visit(el);
        }
    }

//    public static boolean isRmlServiceGroup(Element elt) {
//        boolean result = false;
//        ElementGroup grp = ObjectUtils.castAsOrNull(ElementGroup.class, elt);
//        if (grp != null && !grp.isEmpty()) {
//            ElementService svc = ObjectUtils.castAsOrNull(ElementService.class, grp.getElements().get(0));
//            result = svc != null && isRmlServiceNode(svc.getServiceNode());
//        }
//        return result;
//    }

    public static boolean isRmlServiceNode(Node node) {
        boolean result = node != null && node.isURI() && NorseRmlTerms.RML_SOURCE_SERVICE_IRI.equals(node.getURI());
        return result;
    }

    private static Query wrapAsQuery(Element elt) {
        Query result = new Query();
        result.setQuerySelectType();
        result.setQueryResultStar(true);
        result.setQueryPattern(elt);
        result.setLimit(Long.MAX_VALUE);
        return result;
    }

    /**
     * Add a sub-query to enforce a hash join
     * SELECT * { SERVICE <rml.source> { } }
     * becomes
     * SELECT * { { SELECT * { SERVICE <rml.source> { } } LIMIT LONG.MAX_VALUE } }
     **/
    public static void wrapServiceWithSubQueryInPlace(Query query) {
        Element queryPattern = query.getQueryPattern();

        if (queryPattern instanceof ElementGroup) {
            ElementGroup grp = (ElementGroup)queryPattern;
            List<Element> elts = grp.getElements();
            List<Integer> rmlServiceEltIdxs = IntStream.range(0, elts.size()).mapToObj(i -> {
                Element elt = elts.get(i);
                RmlServiceGroupCounter counter = new RmlServiceGroupCounter();
                ElementWalker.walk(elt, counter);
                int count = counter.getCounter();
                return count > 0 ? i : -1;
            })
            .filter(x -> x >= 0)
            .collect(Collectors.toList());

            if (rmlServiceEltIdxs.size() > 1) {
                for (int eltIdx : rmlServiceEltIdxs) {
                    Element oldElt = elts.get(eltIdx);
                    Query subQuery = null;

                    if (oldElt instanceof ElementSubQuery) {
                        subQuery = ((ElementSubQuery)oldElt).getQuery();
                        if (!subQuery.hasLimit()) {
                            subQuery.setLimit(Long.MAX_VALUE);
                        }
                    }

                    if (subQuery == null) {
                        subQuery = wrapAsQuery(oldElt);
                    }

                    Element newElt = new ElementSubQuery(subQuery);
                    elts.set(eltIdx, newElt);
                }
            }
        }
    }

    /**
     * Given a set of quad patterns return a new one where no pair of quads is in a subsumption relation.
     * O(n^2) implementation - most likely it can be done a lot better.
     * @param quads
     * @return
     */
    public static Set<Quad> getNonSubsumedQuads(Set<Quad> quads) {
        // Copy all quads - then remove all that are subsumed
        Set<Quad> result = new HashSet<>(quads);
        for (Quad x : quads) {
            Iterator<Quad> it = result.iterator();
            while (it.hasNext()) {
                Quad y = it.next();
                if (!x.equals(y) && QuadUtils.matches(x, y)) {
                    it.remove();
                }
            }
        }
        return result;
    }

    class DistinctSchedule {
        Set<Quad> quadsForDistinct;

        // Inner nodes have only children set, leaf nodes only queries
        List<DistinctSchedule> children;
        List<Query> queries;
    }


    // TODO Finish implementation
    /** A greedy algorithm that computes conditional distinct first on the quad patterns that
     * subsume most other quads in the queries.
     */
    public static DistinctSchedule optimizeForDistinct(Cluster<Quad, Query> cluster) {
        // Assumption: no quad in patterns subsumes any other!
        Set<Quad> patterns = cluster.getKeys();
        List<Query> queries = cluster.getValues();

        Multimap<Quad, Query> usages = ArrayListMultimap.create();
        for (Quad pattern : patterns) {
            for (Query query : queries) {
                for (Quad q : query.getConstructTemplate().getQuads()) {
                    if (QuadUtils.matches(pattern, q)) {
                        usages.put(pattern, query);
                    }
                }
            }
        }

        // Get the list of most used quads
        int max = usages.asMap().values().stream().mapToInt(Collection::size).max().orElse(0);
        Set<Quad> maxUsedQuads = usages.asMap().entrySet().stream()
                .filter(e -> e.getValue().size() == max)
                .map(Entry::getKey)
                .collect(Collectors.toSet());

        // Tie break: Pick the cand quad that closes the most other quads
        // If there is still a tie then pick any
        // A quad is closed if there exists no query outside of cand's cluster which it subsumes
        for (Quad cand : maxUsedQuads) {
        }

        return null;
    }

    /**
     * Compute connected components for a set of construct queries:
     * Queries are connected if they may produce overlapping quads.
     * This can be used to optimize distinct operations: Instead of having
     * a single distinct operation at the top of the algebra expression, there can be a union
     * of distinct operations that operate on fewer data.
     */
    public static Clusters<Quad, Query> groupConstructQueriesByTemplate(List<Query> queries) {
        // Map each gp tuple to the id of the query
        Clusters<Quad, Query> clusters = new Clusters<>();

        for (Query query : queries) {
            List<Quad> quads = query.getConstructTemplate().getQuads();
            Set<Integer> affectedClusterIds = new HashSet<>();
            Set<Quad> quadKeys = new HashSet<>();
            for (Quad quad : quads) {
                Quad quadKey = NodeTransformLib.transform(n -> n.isVariable() ? Node.ANY : n, quad);
                quadKeys.add(quadKey);
                for (Entry<Integer, Cluster<Quad, Query>> cluster : clusters.entrySet()) {
                    Set<Quad> clusterKeys = cluster.getValue().getKeys();
                    boolean isSubsumedOrSubsumes = clusterKeys.stream().anyMatch(
                            q -> QuadUtils.matches(q, quadKey) || QuadUtils.matches(quadKey, q));
                    if (isSubsumedOrSubsumes) {
                        affectedClusterIds.add(cluster.getKey());
                    }
                }
            }
            if (quadKeys.isEmpty()) {
                System.err.println("Skipping query due to empty construct template: " + query);
            }

            Cluster<Quad, Query> tgt;
            int targetClusterId;
            if (affectedClusterIds.size() == 1) {
                targetClusterId = affectedClusterIds.iterator().next();
                tgt = clusters.get(targetClusterId);
            } else {
                // TODO: The cluster key should not contain any subsumed quads
                tgt = clusters.newClusterFromMergeOf(affectedClusterIds);
            }
            quadKeys.forEach(tgt.getKeys()::add);
            tgt.getValues().add(query);
        }

        // Post process cluster keys to remove subsumed quads
        for (Cluster<Quad, Query> cluster : clusters.getMap().values()) {
            Set<Quad> cleanKeys = getNonSubsumedQuads(cluster.getKeys());
            cluster.getKeys().clear();
            cluster.getKeys().addAll(cleanKeys);
        }

        return clusters;
    }


    public static void optimizeRmlWorkloadInPlace(List<Query> stmts) {
        // Cluster queries which have the same single source
        ListMultimap<ElementService, Query> sourceToQueries = ArrayListMultimap.create();
        Iterator<Query> it = stmts.iterator();
        while (it.hasNext()) {
            Query query = it.next();

            ElementGroup grp = ObjectUtils.castAsOrNull(ElementGroup.class, query.getQueryPattern());
            if (grp != null && !grp.isEmpty()) {
                ElementService svc = ObjectUtils.castAsOrNull(ElementService.class, grp.get(0));
                if (svc != null) {
                    List<Element> elts = grp.getElements();
                    // Only optimize if ALL elements except for the first are BINDs
                    boolean allBind = elts.subList(1, elts.size()).stream().allMatch(e -> e instanceof ElementBind);

                    if (allBind) {
                        elts.remove(0); // Changes query!
                        sourceToQueries.put(svc, query);
                        it.remove();
                    }
                }
            }
        }

        for (Entry<ElementService, Collection<Query>> e : sourceToQueries.asMap().entrySet()) {
            ElementService sourceElt = e.getKey();

            QuadAcc newQuads = new QuadAcc();
            //List<Element> lateralUnionMembers = new ArrayList<>();
            List<Element> binds = new ArrayList<>();

            // The variable(s) mentioned in the source must not be renamed
            Set<Var> staticVars = SetUtils.asSet(PatternVars.vars(sourceElt));

            int memberId = 0;
            for (Query query : e.getValue()) {
                Element memberElt = query.getQueryPattern();
                Set<Var> mentionedVars = SetUtils.asSet(PatternVars.vars(memberElt));
                Set<Var> toRename = Sets.difference(mentionedVars, staticVars);
                int finalMemberId = memberId;
                Map<Var, Var> remap = toRename.stream()
                        .collect(Collectors.toMap(v -> v, v -> Var.alloc("m" + finalMemberId + "_" + v.getVarName())));
                Query mergeQuery = QueryUtils.applyNodeTransform(query, new NodeTransformSubst(remap));
                //Query mergeQuery = QueryTransformOps.transform(query, remap);

                mergeQuery.getConstructTemplate().getQuads().forEach(newQuads::addQuad);
                // lateralUnionMembers.add(mergeQuery.getQueryPattern());

                // We know that the query pattern only contains bind statements
                ElementGroup grp = (ElementGroup)mergeQuery.getQueryPattern();
                binds.addAll(grp.getElements());

                ++memberId;
            }

            // Element union = ElementUtils.unionIfNeeded(lateralUnionMembers);

            Query q = new Query();
            q.setQueryConstructType();
            q.setConstructTemplate(new Template(newQuads));
            ElementGroup elt = new ElementGroup();
            elt.addElement(sourceElt);
            // elt.addElement(new ElementLateral(union));
            binds.forEach(elt::addElement);
            q.setQueryPattern(elt);
            stmts.add(q);
        }
    }
}
