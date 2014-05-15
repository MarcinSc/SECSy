package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

public interface Client<E> {
    public void sendEvent(EntityRef<E> entity, E event);

    public void sendEntity(EntityRef<E> entity);
}
