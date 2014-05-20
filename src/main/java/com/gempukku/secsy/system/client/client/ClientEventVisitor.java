package com.gempukku.secsy.system.client.client;

import com.gempukku.secsy.entity.io.EntityState;

public interface ClientEventVisitor<E> {
    public void visitEntityUpdate(int entityId, EntityState<E> entityState);
    public void visitEntityRemove(int entityId);
    public void visitEventSend(int entityId, E event);
}
