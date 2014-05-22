package com.gempukku.secsy.system.client.host;

public interface ClientCallback<E> {
    public void sendEvent(int entityId, E event);
}
