package com.gempukku.secsy.system.client;

import java.util.Collection;

public interface ObjectCloud<E> {
    /**
     * If this entity cloud contains this entity.
     * @param entity
     * @return
     */
    public boolean containsEntity(E entity);

    /**
     * Sets dependencies for the specified entity. Also marks the entity as root, if it is one.
     * @param root
     * @param entity
     * @param entities
     * @return
     */
    public Collection<E> setEntityState(boolean root, E entity, Collection<? extends E> entities);

    public Collection<E> getAllEntities();

    public Collection<E> getRootEntities();

    public boolean isRootEntity(E entity);
}
