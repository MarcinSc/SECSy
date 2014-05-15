package com.gempukku.secsy;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityManager<E> {
    public EntityRef<E> create();

    public void destroy(EntityRef<E> entity);

    public int getEntityId(EntityRef<E> entity);
}
