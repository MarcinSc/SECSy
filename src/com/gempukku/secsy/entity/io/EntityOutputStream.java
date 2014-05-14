package com.gempukku.secsy.entity.io;

import com.gempukku.secsy.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityOutputStream<E> {
    public void writeEntity(int entityId, EntityRef<E> entity);
}
