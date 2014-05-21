package com.gempukku.secsy.system;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CompositeSystemProducer<E> implements SystemProducer<E> {
    private SystemProducer<E>[] systemProducers;

    public CompositeSystemProducer(SystemProducer<E> ... systemProducers) {
        this.systemProducers = systemProducers;
    }

    @Override
    public Collection<E> createSystems() {
        Set<E> result = new HashSet<>();
        for (SystemProducer<E> systemProducer : systemProducers) {
            result.addAll(systemProducer.createSystems());
        }

        return result;
    }
}
