package com.gempukku.secsy.entity.io;

import com.gempukku.secsy.EntityRef;

public interface EntityState<E> {
    public void applyState(EntityRef<E> entity);
}
