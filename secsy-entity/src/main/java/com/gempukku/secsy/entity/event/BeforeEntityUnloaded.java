package com.gempukku.secsy.entity.event;

public class BeforeEntityUnloaded extends Event {
    public static final BeforeEntityUnloaded SINGLETON = new BeforeEntityUnloaded();

    private BeforeEntityUnloaded() {}
}
