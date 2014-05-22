package com.gempukku.secsy.system;

import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.EventListener;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShareSystemInitializer<E, S> implements SystemInitializer<S> {
    @Override
    public SystemContext initializeSystems(Collection<S> systems) {
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

        final EventBus<E> eventBus = (EventBus<E>) context.get(EventBus.class);

        for (S system : systems) {
            if (system instanceof EventListener) {
                eventBus.addEventListener((EventListener<E>) system);
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

        return new SimpleSystemContext(context);
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
    public void destroySystems(SystemContext systemContext, Collection<S> systems) {
        final EventBus eventBus = systemContext.getSystem(EventBus.class);
        for (S system : systems) {
            if (system instanceof EventListener) {
                eventBus.removeEventListener((EventListener<E>) system);
            }
        }
    }
}
