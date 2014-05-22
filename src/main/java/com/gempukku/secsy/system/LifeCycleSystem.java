package com.gempukku.secsy.system;

public interface LifeCycleSystem {
    public void preInitialize();

    public void initialize();

    public void postInitialize();

    public void preUpdate();

    public void update(long delta);

    public void postUpdate();

    public void beforeDestroy();

    public void destroy();

    public void postDestroy();
}
