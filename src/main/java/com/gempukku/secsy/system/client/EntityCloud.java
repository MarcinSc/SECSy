package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;

public interface EntityCloud<E> {
    /**
     * If this entity cloud contains this entity.
     * @param entity
     * @return
     */
    public boolean containsEntity(EntityRef<E> entity);

    /**
     * Sets dependencies for the specified entity. Also marks the entity as root, if it is one.
     * @param root
     * @param entity
     * @param entities
     * @return
     */
    public Collection<EntityRef<E>> setEntityState(boolean root, EntityRef<E> entity, Collection<EntityRef<E>> entities);

    /**
     * Removes the entity (and all un-attached non-root dependent entities), returns a collection of entities that were
     * removed.
     * @param entity
     * @return
     */
    public Collection<EntityRef<E>> removeEntity(EntityRef<E> entity);
}
