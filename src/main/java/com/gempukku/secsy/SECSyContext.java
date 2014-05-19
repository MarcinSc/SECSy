package com.gempukku.secsy;

import com.gempukku.secsy.system.SystemInitializer;
import com.gempukku.secsy.system.SystemProducer;

import java.util.Collection;

public class SECSyContext<S, E> {
    private SystemProducer<S> systemProducer;
    private SystemInitializer<S> systemInitializer;
    private EventBus<E> eventBus;

    public SECSyContext(SystemProducer<S> systemProducer, SystemInitializer<S> systemInitializer, EventBus<E> eventBus) {
        this.systemProducer = systemProducer;
        this.systemInitializer = systemInitializer;
        this.eventBus = eventBus;
    }

    public void start() {
        final Collection<S> systems = systemProducer.getSystems();
        for (S system : systems) {
            if (system instanceof EventListener) {
                eventBus.addEventListener((EventListener<E>) system);
            }
        }

        systemInitializer.initializeSystems(systems);
    }
}
