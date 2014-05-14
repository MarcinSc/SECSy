package org.terasology.ecs;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityFactory<E> {
    public EntityRef<E> createEntity();
    public void setEntityListener(EntityRef<E> entity, EntityListener<E> entityListener);
    public void destroyEntity(EntityRef<E> entity);
}
