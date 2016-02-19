package com.gempukku.secsy.context.system;

import com.gempukku.secsy.context.SystemContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleContext<S> implements SystemContext {
    private SystemProducer<S> systemProducer;
    private SystemInitializer<S> systemInitializer;

    private List<LifeCycleSystem> lifeCycleSystems = new LinkedList<>();

    private Collection<S> systems;
    private Map<Class<?>, S> systemMap;

    public void setSystemProducer(SystemProducer<S> systemProducer) {
        this.systemProducer = systemProducer;
    }

    public void setSystemInitializer(SystemInitializer<S> systemInitializer) {
        this.systemInitializer = systemInitializer;
    }

    public void startup() {
        systems = systemProducer.createSystems();
        for (S system : systems) {
            if (system instanceof LifeCycleSystem) {
                lifeCycleSystems.add((LifeCycleSystem) system);
            }
        }

        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.preInitialize();
        }

        systemMap = systemInitializer.initializeSystems(systems);

        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.initialize();
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.postInitialize();
        }
    }

    @Override
    public <T> T getSystem(Class<T> clazz) {
        if (systemMap == null)
            return null;

        return (T) systemMap.get(clazz);
    }

    public void shutdown() {
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.preDestroy();
        }
        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.destroy();
        }

        systemInitializer.destroySystems(systems);

        for (LifeCycleSystem lifeCycleSystem : lifeCycleSystems) {
            lifeCycleSystem.postDestroy();
        }

        systems = null;
        systemMap = null;
    }
}