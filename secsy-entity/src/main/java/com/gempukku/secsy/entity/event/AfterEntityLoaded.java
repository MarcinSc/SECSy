package com.gempukku.secsy.entity.event;

public class AfterEntityLoaded extends Event {
    public static final AfterEntityLoaded SINGLETON = new AfterEntityLoaded();

    private AfterEntityLoaded() {}
}
