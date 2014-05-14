package org.terasology.ecs.storage;

import org.terasology.ecs.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityOutputStream<E> {
    public void writeEntity(int entityId, EntityRef<E> entity);
}
