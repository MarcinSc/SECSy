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
            Class<? extends Object> systemClass = system.getClass();
            while (true) {
                initForClass(context, system, systemClass);
                systemClass = systemClass.getSuperclass();
                if (systemClass == Object.class) {
                    break;
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

    private void initForClass(Map<Class<?>, Object> context, S system, Class<? extends Object> systemClass) {
        for (Field field : systemClass.getFields()) {
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

    @Override
    public void destroySystems(Collection<S> systems) {
        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                ((LifeCycleSystem) system).beforeDestroy();
            }
        }

        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                ((LifeCycleSystem) system).destroy();
            }
        }

        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                ((LifeCycleSystem) system).postDestroy();
            }
        }
    }
}
