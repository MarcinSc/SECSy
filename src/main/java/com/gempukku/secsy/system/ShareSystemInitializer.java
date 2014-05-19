package com.gempukku.secsy.system;

import com.gempukku.secsy.EventBus;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShareSystemInitializer<Event, S> implements SystemInitializer<S> {
    private EventBus<Event> eventBus;
    private Map<Class<?>, Object> context = new HashMap<>();

    public ShareSystemInitializer(EventBus<Event> eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void initializeSystems(Collection<S> systems) {
        for (S system : systems) {
            final Share shared = system.getClass().getAnnotation(Share.class);
            if (shared != null) {
                for (Class<?> clazz : shared.value()) {
                    context.put(clazz, system);
                }
            }
        }

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
