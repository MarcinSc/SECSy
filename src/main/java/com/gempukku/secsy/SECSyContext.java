package com.gempukku.secsy;

import com.gempukku.secsy.system.SystemInitializer;
import com.gempukku.secsy.system.SystemProducer;

import java.util.Collection;

public class SECSyContext<S, E> {
    private SystemProducer<S> systemProducer;
    private SystemInitializer<S> systemInitializer;
    private EventBus<E> eventBus;

    private Collection<S> systems;

    public SECSyContext(SystemProducer<S> systemProducer, SystemInitializer<S> systemInitializer, EventBus<E> eventBus) {
        this.systemProducer = systemProducer;
        this.systemInitializer = systemInitializer;
        this.eventBus = eventBus;
    }

    public void startup() {
        systems = systemProducer.getSystems();
        for (S system : systems) {
            if (system instanceof EventListener) {
                eventBus.addEventListener((EventListener<E>) system);
            }
        }

        systemInitializer.initializeSystems(systems);
    }

    public void shutdown() {
        systemInitializer.destroySystems(systems);

        for (S system : systems) {
            if (system instanceof EventListener) {
                eventBus.removeEventListener((EventListener<E>) system);
            }
        }
    }
}
