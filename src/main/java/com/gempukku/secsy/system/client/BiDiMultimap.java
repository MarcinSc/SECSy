package com.gempukku.secsy.system.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;

public class BiDiMultimap<K, V> {
    private Multimap<K, V> directionOne = HashMultimap.create();
    private Multimap<V, K> directionTwo = HashMultimap.create();

    public void put(K k, V v) {
        directionOne.put(k, v);
        directionTwo.put(v, k);
    }

    public void remove(K k, V v) {
        directionOne.remove(k, v);
        directionTwo.remove(v, k);
    }

    public Collection<? extends V> getAllValues() {
        return Collections.unmodifiableCollection(directionOne.values());
    }

    public Collection<? extends K> getAllKeys() {
        return Collections.unmodifiableCollection(directionTwo.values());
    }

    public Collection<? extends V> getValues(K k) {
        return Collections.unmodifiableCollection(directionOne.get(k));
    }

    public Collection<? extends K> getKeys(V v) {
        return Collections.unmodifiableCollection(directionTwo.get(v));
    }

    public boolean containsKey(K k) {
        return directionOne.containsKey(k);
    }

    public boolean containsValue(V v) {
        return directionTwo.containsKey(v);
    }

    public Collection<V> removeAllValues(K k) {
        final Collection<V> values = directionOne.removeAll(k);
        for (V v : values) {
            directionTwo.remove(v, k);
        }
        return values;
    }

    public Collection<K> removeAllKeys(V v) {
        final Collection<K> keys = directionTwo.removeAll(v);
        for (K k : keys) {
            directionOne.remove(k, v);
        }
        return keys;
    }
}
