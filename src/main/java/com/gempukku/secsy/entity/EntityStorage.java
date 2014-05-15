package com.gempukku.secsy.entity;

import com.gempukku.secsy.EntityRef;

public interface EntityStorage<E> {
    int storeNewEntity(EntityRef<E> entity);

    void storeEntityWithId(int entityId, EntityRef<E> entity);

    void removeEntity(EntityRef<E> entity);

    int getEntityId(EntityRef<E> entity);

    EntityRef<E> getEntityById(int id);
}
