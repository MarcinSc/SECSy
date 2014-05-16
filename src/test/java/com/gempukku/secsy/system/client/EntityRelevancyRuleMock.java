package com.gempukku.secsy.system.client;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityRelevancyRuleMock<E> implements EntityRelevancyRule<E> {
    private Map<EntityRef<E>, Collection<EntityRef<E>>> relevantClientEntities = new HashMap<>();
    private Class<? extends E> impactingEvent;

    public void setClientRelevantEntities(EntityRef<E> clientEntity, Collection<EntityRef<E>> relevantEntities) {
        relevantClientEntities.put(clientEntity, relevantEntities);
    }

    public void setImpactingEvent(Class<? extends E> impactingEvent) {
        this.impactingEvent = impactingEvent;
    }

    @Override
    public Collection<EntityRef<E>> listEntitiesToTrackDueToImpactingEvent(EntityRef<E> clientEntity, EntityRef<E> entity) {
        final Collection<EntityRef<E>> entityRefs = relevantClientEntities.get(clientEntity);
        if (entityRefs == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(entity);
    }

    @Override
    public Collection<EntityRef<E>> listRelevantEntities(EntityRef<E> clientEntity) {
        final Collection<EntityRef<E>> entityRefs = relevantClientEntities.get(clientEntity);
        if (entityRefs == null) {
            return Collections.emptySet();
        }
        return entityRefs;
    }

    @Override
    public boolean isRelevanceImpactingEvent(E event) {
        return impactingEvent != null && impactingEvent == event.getClass();
    }

    @Override
    public boolean isComponentRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Component component) {
        return false;
    }

    @Override
    public boolean isComponentFieldRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Component component, String field) {
        return false;
    }
}
