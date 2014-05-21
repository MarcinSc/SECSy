package com.gempukku.secsy.system.client;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.system.client.host.EntityRelevancyRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityRelevancyRuleMock<E> implements EntityRelevancyRule<E> {
    private Map<EntityRef<E>, Collection<EntityRef<E>>> relevantClientEntities = new HashMap<>();
    private Map<EntityRef<E>, Map<EntityRef<E>, Collection<EntityRef<E>>>> relevantDependentEntitiesForClient = new HashMap<>();
    private Class<? extends E> impactingEvent;

    public void setClientDirectlyRelevantEntities(EntityRef<E> clientEntity, Collection<EntityRef<E>> relevantEntities) {
        relevantClientEntities.put(clientEntity, relevantEntities);
    }

    public void setClientDependentRelevantEntities(EntityRef<E> clientEntity, EntityRef<E> entity, Collection<EntityRef<E>> dependentEntities) {
        Map<EntityRef<E>, Collection<EntityRef<E>>> dependencyMap = relevantDependentEntitiesForClient.get(clientEntity);
        if (dependencyMap == null) {
            dependencyMap = new HashMap<>();
            relevantDependentEntitiesForClient.put(clientEntity, dependencyMap);
        }
        dependencyMap.put(entity, dependentEntities);
    }

    public void setImpactingEvent(Class<? extends E> impactingEvent) {
        this.impactingEvent = impactingEvent;
    }

    @Override
    public Collection<EntityRef<E>> listDirectlyRelevantEntities(EntityRef<E> clientEntity) {
        final Collection<EntityRef<E>> entityRefs = relevantClientEntities.get(clientEntity);
        if (entityRefs == null) {
            return Collections.emptySet();
        }
        return entityRefs;
    }

    @Override
    public boolean isEntityDirectlyRelevant(EntityRef<E> clientEntity, EntityRef<E> entity) {
        final Collection<EntityRef<E>> relevancies = relevantClientEntities.get(clientEntity);
        if (relevancies == null) {
            return false;
        }
        return relevancies.contains(entity);
    }

    @Override
    public Collection<EntityRef<E>> listDependentRelevantEntities(EntityRef<E> clientEntity, EntityRef<E> entity) {
        final Map<EntityRef<E>, Collection<EntityRef<E>>> dependencyMap = relevantDependentEntitiesForClient.get(clientEntity);
        if (dependencyMap == null) {
            return Collections.emptySet();
        }
        final Collection<EntityRef<E>> result = dependencyMap.get(entity);
        if (result == null) {
            return Collections.emptySet();
        }
        return result;
    }

    @Override
    public boolean isRelevanceImpactingEvent(E event) {
        return impactingEvent != null && impactingEvent == event.getClass();
    }

    @Override
    public boolean isComponentRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Class<? extends Component> component) {
        return false;
    }

    @Override
    public boolean isComponentFieldRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Class<? extends Component> component, String field) {
        return false;
    }
}
