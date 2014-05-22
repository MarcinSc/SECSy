package com.gempukku.secsy;

import com.gempukku.secsy.system.LifeCycleSystem;
import com.gempukku.secsy.system.SystemContext;
import com.gempukku.secsy.system.SystemInitializer;
import com.gempukku.secsy.system.SystemProducer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SECSyContext<S, E> {
    private SystemProducer<S> systemProducer;
    private SystemInitializer<S> systemInitializer;

    private List<LifeCycleSystem> lifeCycleSystems = new LinkedList<>();

    private Collection<S> systems;
    private SystemContext systemContext;

    public SECSyContext(SystemProducer<S> systemProducer, SystemInitializer<S> systemInitializer) {
        this.systemProducer = systemProducer;
        this.systemInitializer = systemInitializer;
    }

    public void startup() {
        systems = systemProducer.createSystems();
        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                lifeCycleSystems.add((LifeCycleSystem) system);
            }
        }

        systemContext = systemInitializer.initializeSystems(systems);

        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.preInitialize();
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.initialize();
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.postInitialize();
        }
    }

    public void update(long delta) {
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.preUpdate();
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.update(delta);
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.postUpdate();
        }
    }

    public void shutdown() {
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.preDestroy();
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.destroy();
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.postDestroy();
        }

        systemInitializer.destroySystems(systemContext, systems);
    }
}
