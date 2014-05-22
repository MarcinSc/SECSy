package com.gempukku.secsy.system.client;

public interface ContextEventFilter<E> {
    public boolean isToServerEvent(E event);
    public boolean isToClientEvent(E event);
}
