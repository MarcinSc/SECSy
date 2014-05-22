package com.gempukku.secsy.entity.storage;

import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.entity.EntityStorage;
import com.gempukku.secsy.entity.NullEntityRef;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class MapEntityStorage<E> implements EntityStorage<E> {
    // Bi-di map
    private Map<Integer, EntityRef<E>> entities = new HashMap<>();
    private Map<EntityRef<E>, Integer> entityIds = new HashMap<>();

    private int lastUsedId = 0;

    @Override
    public int storeNewEntity(EntityRef<E> entity) {
        int entityId = ++lastUsedId;
        entities.put(entityId, entity);
        entityIds.put(entity, entityId);
        return entityId;
    }

    @Override
    public void storeEntityWithId(int entityId, EntityRef<E> entity) {
        entities.put(entityId, entity);
        entityIds.put(entity, entityId);
    }

    @Override
    public void removeEntity(EntityRef<E> entity) {
        Integer entityId = entityIds.remove(entity);
        entities.remove(entityId);
    }

    @Override
    public boolean hasEntity(EntityRef<E> entity) {
        return entityIds.containsKey(entity);
    }

    @Override
    public int getEntityId(EntityRef<E> entity) {
        return entityIds.get(entity);
    }

    @Override
    public EntityRef<E> getEntityById(int id) {
        final EntityRef<E> entity = entities.get(id);
        if (entity == null) {
            return NullEntityRef.singleton;
        }
        return entity;
    }
}
