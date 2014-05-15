package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;
import java.util.Set;

public interface Client<E> {
    public void updateEntity(int entityId, EntityRef<E> entity, Collection<? extends EntityComponentFieldFilter<E>> componentFieldFilters);

    public void removeEntity(int entityId);

    public void sendEvent(int entityId, EntityRef<E> entity, E event);
}
