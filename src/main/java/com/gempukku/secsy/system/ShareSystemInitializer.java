package com.gempukku.secsy.system;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShareSystemInitializer<Event, S> implements SystemInitializer<S> {
    @Override
    public void initializeSystems(Collection<S> systems) {
        Map<Class<?>, Object> context = new HashMap<>();

        // Figure out shared objects
        for (S system : systems) {
            final Share shared = system.getClass().getAnnotation(Share.class);
            if (shared != null) {
                for (Class<?> clazz : shared.value()) {
                    context.put(clazz, system);
                }
            }
        }

        // Enrich systems with shared components
        for (S system : systems) {
            for (Field field : system.getClass().getFields()) {
                final In in = field.getAnnotation(In.class);
                if (in != null) {
                    final Object value = context.get(field.getType());
                    field.setAccessible(true);
                    try {
                        field.set(system, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // Initialization of LifeCycleSystems
        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                ((LifeCycleSystem) system).preInitialize();
            }
        }

        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                ((LifeCycleSystem) system).initialize();
            }
        }

        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                ((LifeCycleSystem) system).postInitialize();
            }
        }
    }
}
