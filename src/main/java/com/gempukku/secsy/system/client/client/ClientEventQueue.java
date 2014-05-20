package com.gempukku.secsy.system.client.client;

public interface ClientEventQueue<E> {
    public void visitQueuedEvents(ClientEventVisitor<E> visitor);
}
