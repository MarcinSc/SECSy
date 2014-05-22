package com.gempukku.secsy.system;

public interface SystemContext {
    public <T> T getSystem(Class<T> clazz);
}
