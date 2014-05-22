package com.gempukku.secsy.system.client.client;

import com.gempukku.secsy.entity.io.EntityState;
import com.gempukku.secsy.system.client.host.ClientCallback;

public interface ClientEventQueue<E> {
    public void visitQueuedEvents(ClientEventVisitor<E> visitor);

    public void setClientCallback(ClientCallback<E> clientCallback);

    public void sendServerEvent(int entityId, E event);

    public interface ClientEventVisitor<E> {
        public void visitEntityUpdate(int entityId, EntityState<E> entityState);

        public void visitEntityRemove(int entityId);

        public void visitEventSend(int entityId, E event);
    }
}
