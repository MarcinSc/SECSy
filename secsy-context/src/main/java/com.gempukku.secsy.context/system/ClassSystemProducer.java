package com.gempukku.secsy.context.system;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClassSystemProducer implements SystemProducer {
    private Set<Class<?>> systemClasses = new HashSet<Class<?>>();

    public void addClass(Class<?> systemClass) {
        systemClasses.add(systemClass);
    }

    @Override
    public Iterable<Object> createSystems() {
        try {
            Set<Object> systems = new HashSet<Object>();

            for (Class<?> system : systemClasses) {
                systems.add(system.newInstance());
            }

            return Collections.unmodifiableCollection(systems);
        } catch (IllegalAccessException exp) {
            throw new RuntimeException("Unable to instantiate systems", exp);
        } catch (InstantiationException exp) {
            throw new RuntimeException("Unable to instantiate systems", exp);
        }
    }
}
