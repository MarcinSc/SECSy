package com.gempukku.secsy.system;

import java.util.Collection;
import java.util.HashSet;

public class ClassSystemProducer<E> implements SystemProducer<E> {
    private Class<? extends E>[] systems;

    public ClassSystemProducer(Class<? extends E>... systems) {
        this.systems = systems;
    }

    @Override
    public Collection<E> createSystems() {
        HashSet<E> result = new HashSet<>();
        for (Class<? extends E> system : systems) {
            try {
                result.add(system.newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
