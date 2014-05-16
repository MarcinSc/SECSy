package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;

public interface EntityRelevancyRule<E> extends EntityComponentFieldFilter<E> {
    public Collection<EntityRef<E>> listRelevantEntities(EntityRef<E> clientEntity);

    public boolean isEntityRelevant(EntityRef<E> clientEntity, EntityRef<E> entity);

    public boolean isRelevanceImpactingEvent(E event);
}