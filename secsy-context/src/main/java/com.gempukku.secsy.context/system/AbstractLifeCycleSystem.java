package com.gempukku.secsy.context.system;

public abstract class AbstractLifeCycleSystem implements LifeCycleSystem {
    @Override
    public void preInitialize() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void postInitialize() {

    }

    @Override
    public void preDestroy() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void postDestroy() {

    }

    @Override
    public float getPriority() {
        return 0;
    }
}
