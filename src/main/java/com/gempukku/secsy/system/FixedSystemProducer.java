package com.gempukku.secsy.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class FixedSystemProducer<E> implements SystemProducer<E> {
    private E[] systems;

    public FixedSystemProducer(E... systems) {
        this.systems = systems;
    }

    @Override
    public Collection<E> getSystems() {
        return new HashSet<>(Arrays.asList(systems));
    }
}
