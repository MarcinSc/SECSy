package com.gempukku.secsy.system.client.client;

import com.gempukku.secsy.Component;

import java.util.Set;

public interface ClientEventVisitor<E> {
    public void visitEntityUpdate(int entityId, EntityState<E> entityState);
    public void visitEntityRemove(int entityId);
    public void visitEventSend(int entityId, E event);
}
