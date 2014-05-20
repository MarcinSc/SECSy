package com.gempukku.secsy.system.client.host;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;

public interface Client<E> {
    public void updateEntity(int entityId, EntityRef<E> entity, Collection<? extends EntityComponentFieldFilter<E>> componentFieldFilters);

    public void removeEntity(int entityId);

    public void sendEvent(int entityId, EntityRef<E> entity, E event);
}
