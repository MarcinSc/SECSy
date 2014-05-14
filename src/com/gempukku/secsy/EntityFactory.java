package com.gempukku.secsy;

import com.gempukku.secsy.entity.EntityListener;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityFactory<E> {
    public EntityRef<E> createEntity(EntityListener<E> entityListener);

    public void setEntityListener(EntityRef<E> entity, EntityListener<E> entityListener);

    public void destroyEntity(EntityRef<E> entity);
}
