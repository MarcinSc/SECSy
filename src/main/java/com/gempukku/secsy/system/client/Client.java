package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

public interface Client<E> {
    public void updateEntity(int entityId, EntityRef<E> entity);

    public void removeEntity(int entityId);

    public void sendEvent(int entityId, EntityRef<E> entity, E event);
}
