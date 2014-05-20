package com.gempukku.secsy.system.client.client;

import com.gempukku.secsy.EntityRef;

public interface EntityState<E> {
    public void applyState(EntityRef<E> entity);
}
