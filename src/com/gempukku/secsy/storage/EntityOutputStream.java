package com.gempukku.secsy.storage;

import com.gempukku.secsy.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityOutputStream<E> {
    public void writeEntity(int entityId, EntityRef<E> entity);
}
