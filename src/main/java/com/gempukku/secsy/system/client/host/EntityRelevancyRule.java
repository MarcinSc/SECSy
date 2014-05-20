package com.gempukku.secsy.system.client.host;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;

public interface EntityRelevancyRule<E> extends EntityComponentFieldFilter<E> {
    public Collection<EntityRef<E>> listDirectlyRelevantEntities(EntityRef<E> clientEntity);
    
    public Collection<EntityRef<E>> listDependentRelevantEntities(EntityRef<E> clientEntity, EntityRef<E> entity);

    public boolean isEntityDirectlyRelevant(EntityRef<E> clientEntity, EntityRef<E> entity);

    public boolean isRelevanceImpactingEvent(E event);
}
