package com.gempukku.secsy.entity.io;

import com.gempukku.secsy.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityInputStream<E> {
    public EntityWithId<E> readEntity();

    public class EntityWithId<E> {
        public final int id;
        public final EntityRef<E> entity;

        public EntityWithId(int id, EntityRef<E> entity) {
            this.id = id;
            this.entity = entity;
        }
    }
}
