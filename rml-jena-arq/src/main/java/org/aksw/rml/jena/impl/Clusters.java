package org.aksw.rml.jena.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public class Clusters<K, V> {
    protected NavigableMap<Integer, Clusters.Cluster<K, V>> map = new TreeMap<>();

    public static class Cluster<K, V> {
        protected Set<K> keys = new LinkedHashSet<>();
        protected List<V> values = new ArrayList<>();

//        public Cluster(K key, V value) {
//            keys.add(key);
//            values.add(value);
//        }

        public Set<K> getKeys() {
            return keys;
        }
        public List<V> getValues() {
            return values;
        }

        public void mergeFrom(Clusters.Cluster<K, V> other) {
            keys.addAll(other.getKeys());
            values.addAll(other.getValues());
        }
    }

    public int getNextId() {
        int result = Optional.ofNullable(map.lastEntry()).map(Entry::getKey).map(v -> v + 1).orElse(0);
        return result;
    }

    public NavigableMap<Integer, Clusters.Cluster<K, V>> getMap() {
        return getMap();
    }

    public Set<Entry<Integer, Clusters.Cluster<K, V>>> entrySet() {
        return map.entrySet();
    }

    public Clusters.Cluster<K, V> get(int id) {
        return map.get(id);
    }

    public Clusters.Cluster<K, V> newClusterFromMergeOf(Set<Integer> affectedClusterIds) {
        // Create a new cluster and merge any prior ones
        int targetClusterId = getNextId();
        Clusters.Cluster<K, V> tgt = new Clusters.Cluster<>();
        for (int srcId : affectedClusterIds) {
            Clusters.Cluster<K, V> src = map.remove(srcId);
            tgt.mergeFrom(src);
        }
        map.put(targetClusterId, tgt);
        return tgt;
    }

//    public Collection<Cluster<K, V>> entries() {
//        return map.values();
//    }
}
