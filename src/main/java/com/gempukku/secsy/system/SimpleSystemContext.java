package com.gempukku.secsy.system;

import java.util.Map;

public class SimpleSystemContext implements SystemContext {
    private Map<Class<?>, Object> systems;

    public SimpleSystemContext(Map<Class<?>, Object> systems) {
        this.systems = systems;
    }

    @Override
    public <T> T getSystem(Class<T> clazz) {
        return (T) systems.get(clazz);
    }
}
