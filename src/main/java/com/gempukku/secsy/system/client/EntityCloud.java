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

    public Collection<EntityRef<E>> getAllEntities();

    public Collection<EntityRef<E>> getRootEntities();

    public boolean isRootEntity(EntityRef<E> entity);
}
