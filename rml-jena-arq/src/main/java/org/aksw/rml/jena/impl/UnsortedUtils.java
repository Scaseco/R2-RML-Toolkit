package org.aksw.rml.jena.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.aksw.commons.algebra.allen.AllenConstants;
import org.aksw.commons.algebra.allen.AllenRelation;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.constraint.api.CBinding;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.impl.CBindingMap;
import org.aksw.jenax.constraint.util.ConstraintDerivations;
import org.aksw.jenax.sparql.algebra.eval.EvaluationOfConstraints;
import org.aksw.jenax.sparql.algebra.eval.Evaluator;
import org.aksw.rml.jena.impl.Clusters.Cluster;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprLib;
import org.junit.Assert;

import com.google.common.base.Preconditions;

public class UnsortedUtils {

    public static Map<Quad, Tuple<VSpace>> analyzeQuads(Query query) {
        Preconditions.checkArgument(query.isConstructType(), "Query must be of CONSTRUCT type");

        Op op = Algebra.compile(query);
        System.err.println(op);
        // op = TransformScopeRename.transform(op) ;
        Evaluator<CBinding> evaluator = new EvaluationOfConstraints();
        CBinding cbinding = evaluator.evalOp(op, CBindingMap.create());

        Objects.requireNonNull(cbinding);

        List<Quad> quads = query.getConstructTemplate().getQuads();
        Map<Quad, Tuple<VSpace>> result = new LinkedHashMap<>();
        VSpace[] ctuple = new VSpace[4];
        for (Quad quad : quads) {
            for (int i = 0; i < 4; ++i) {
                Node node = QuadUtils.getNode(quad, i);
                // Objects.requireNonNull(node);

                VSpace vspace;
                if (node.isVariable()) {
                    vspace = cbinding.get((Var)node);
                } else {
                    vspace = ConstraintDerivations.deriveValueSpace(ExprLib.nodeToExpr(node), null);
                }
                Objects.requireNonNull(vspace);

                ctuple[i] = vspace;
            }

            result.put(quad, TupleFactory.create(ctuple));
        }

        return result;
    }

    public static List<Tuple<VSpace>> queryToVSpaces(Query query) {
        Map<Quad, Tuple<VSpace>> cquads = analyzeQuads(query);
        return new ArrayList<>(cquads.values());
    }

    public static int compare(VSpace x, VSpace y) {
        int result = 0;
        AllenRelation ar = x.relateTo(y);
        if ((ar.getPattern() & AllenConstants.BEFORE | AllenConstants.MEETS) != 0) {
            result = -1;
        } else if ((ar.getPattern() & AllenConstants.AFTER | AllenConstants.METBY) != 0) {
            result = 1;
        }
        return result;
    }

    /** Create a comparator for a tuple by comparing by component */
    public static <T> Comparator<Tuple<T>> newTupleComparator(Comparator<T> componentComparator) {
        return (x, y) -> compare(x, y, componentComparator);
    }

    public static <T> int compare(Tuple<T> x, Tuple<T> y, Comparator<T> comparator) {
        int result;
        if (x.len() < y.len()) {
            result = -1;
        } else if (y.len() < x.len()) {
            result = 1;
        } else {
            result = 0;
            for (int i = 0; i < x.len(); ++i) {
                T xv = x.get(i);
                T yv = y.get(i);
                result = comparator.compare(xv, yv);
                if (result != 0) {
                    break;
                }
            }
        }
        return result;
    }

    public static int compare(Tuple<VSpace> x, Tuple<VSpace> y) {
        int result = compare(x, y, UnsortedUtils::compare);
        return result;
    }

//    public static Clusters<Tuple<VSpace>, Query> group(List<Query> queries) {
//        return groupConstructQueriesByTemplate(queries, UnsortedUtils::queryToVSpaces, UnsortedUtils::isSubsumed);
//    }
//
//    public static <T, K> Clusters<K, T> groupConstructQueriesByTemplate(List<T> items, Function<T, List<K>> itemToKeys, Comparator<K> comparator) {
//        // Map each gp tuple to the id of the query
//        Clusters<K, T> clusters = new Clusters<>();
//        for (T item : items) {
//            List<K> itemKeys = itemToKeys.apply(item); // query.getConstructTemplate().getQuads();
//            Set<Integer> affectedClusterIds = new HashSet<>();
//            // Set<K> quadKeys = new HashSet<>();
//            for (K itemKey : itemKeys) {
//                // Quad quadKey = NodeTransformLib.transform(n -> n.isVariable() ? Node.ANY : n, quad);
//                // C quadKey = toKey.apply(key);
//                // quadKeys.add(quadKey);
//                for (Entry<Integer, Cluster<K, T>> cluster : clusters.entrySet()) {
//                    Set<K> clusterKeys = cluster.getValue().getKeys();
//                    boolean isSubsumedOrSubsumes = clusterKeys.stream().anyMatch(
//                            clusterKey -> {
//                                boolean r = isSubsumed.test(clusterKey, itemKey);
//                                r = r || isSubsumed.test(itemKey, clusterKey);
//                                return r;
//                            });
//                    // QuadUtils.matches(q, quadKey) || QuadUtils.matches(quadKey, q));
//                    if (isSubsumedOrSubsumes) {
//                        affectedClusterIds.add(cluster.getKey());
//                    }
//                }
//            }
//            if (itemKeys.isEmpty()) {
//                System.err.println("Skipping item due to empty set of keys: " + item);
//            }
//
//            Cluster<K, T> tgt;
//            int targetClusterId;
//            if (affectedClusterIds.size() == 1) {
//                targetClusterId = affectedClusterIds.iterator().next();
//                tgt = clusters.get(targetClusterId);
//            } else {
//                // TODO: The cluster key should not contain any subsumed quads
//                tgt = clusters.newClusterFromMergeOf(affectedClusterIds);
//            }
//            itemKeys.forEach(tgt.getKeys()::add);
//            tgt.getValues().add(item);
//        }
//
//        // Post process cluster keys to remove subsumed quads
//        for (Cluster<K, T> cluster : clusters.getMap().values()) {
//            Set<K> cleanKeys = getNonSubsumedItems(cluster.getKeys(), isSubsumed);
//            cluster.getKeys().clear();
//            cluster.getKeys().addAll(cleanKeys);
//        }
//
//        return clusters;
//    }
//
//
//    /**
//     * Given a set of quad patterns return a new one where no pair of quads is in a subsumption relation.
//     * O(n^2) implementation - most likely it can be done a lot better.
//     * @param quads
//     * @return
//     */
//    public static <K> Set<K> getNonSubsumedItems(Set<K> items, BiPredicate<K, K> isSubsumed) {
//        // Copy all quads - then remove all that are subsumed
//        Set<K> result = new HashSet<>(items);
//        for (K x : items) {
//            Iterator<K> it = result.iterator();
//            while (it.hasNext()) {
//                K y = it.next();
//                if (!x.equals(y) && isSubsumed.test(x, y)) { // QuadUtils.matches(x, y)) {
//                    it.remove();
//                }
//            }
//        }
//        return result;
//    }

}
