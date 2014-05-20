package com.gempukku.secsy.system;

public interface LifeCycleSystem {
    public void preInitialize();

    public void initialize();

    public void postInitialize();

    public void beforeDestroy();

    public void destroy();

    public void postDestroy();
}
