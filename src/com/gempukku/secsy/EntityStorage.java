package com.gempukku.secsy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class EntityStorage<E> {
    // Bi-di map
    private Map<Integer, EntityRef<E>> entities = new HashMap<>();
    private Map<EntityRef<E>, Integer> entityIds = new HashMap<>();

    private int lastUsedId = 0;

    public int storeNewEntity(EntityRef<E> entity) {
        int entityId = ++lastUsedId;
        entities.put(entityId, entity);
        entityIds.put(entity, entityId);
        return entityId;
    }

    public void storeEntityWithId(int entityId, EntityRef<E> entity) {
        entities.put(entityId, entity);
        entityIds.put(entity, entityId);
    }

    public void removeEntity(EntityRef<E> entity) {
        Integer entityId = entityIds.remove(entity);
        entities.remove(entityId);
    }

    public int getEntityId(EntityRef<E> entity) {
        return entityIds.get(entity);
    }
}
