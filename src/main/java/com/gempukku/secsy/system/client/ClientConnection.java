package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientConnection<E> {
    private Client<E> client;
    private EntityRef<E> clientEntity;
    private Multimap<EntityRef<E>, EntityRelevancyRule<E>> trackingRules;
    private Map<EntityRelevancyRule<E>, ObjectCloud<EntityRef<E>>> trackedEntities;

    public ClientConnection(Client<E> client, EntityRef<E> clientEntity) {
        this.client = client;
        this.clientEntity = clientEntity;
        this.trackingRules = HashMultimap.create();
        this.trackedEntities = new HashMap<>();
    }

    public void addEntityRelevancyRules(EntityManager<E> entityManager, Collection<EntityRelevancyRule<E>> entityRelevancyRules) {
        Set<EntityRef<E>> entitiesToUpdate = new HashSet<>();

        // Send any entities that are relevant to the client
        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            trackedEntities.put(entityRelevancyRule, new SimpleObjectCloud<EntityRef<E>>());
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listDirectlyRelevantEntities(clientEntity)) {
                entitiesToUpdate.add(relevantEntity);
                processEntityGraph(entityRelevancyRule, trackedEntities.get(entityRelevancyRule), true, relevantEntity, entitiesToUpdate);
            }
        }

        for (EntityRef<E> entity : entitiesToUpdate) {
            updateEntity(trackingRules.get(entity), entity, entityManager.getEntityId(entity));
        }
    }

    public void removeEntityRelevancyRules(EntityManager<E> entityManager, EntityRelevancyRule<E> entityRelevancyRule) {
        final ObjectCloud<EntityRef<E>> entityCloud = trackedEntities.get(entityRelevancyRule);
        final Collection<EntityRef<E>> changedEntities = entityCloud.getAllEntities();

        for (EntityRef<E> entity : changedEntities) {
            trackingRules.remove(entity, entityRelevancyRule);
        }

        for (EntityRef<E> relevantEntity : changedEntities) {
            final int entityId = entityManager.getEntityId(relevantEntity);
            updateOrRemoveEntity(relevantEntity, entityId, trackingRules.get(relevantEntity));
        }
    }

    public void entityRelevanceRuleUpdated(EntityManager<E> entityManager, EntityRelevancyRule<E> entityRelevancyRule) {
        Set<EntityRef<E>> entitiesToAdd = new HashSet<>();

        // Recheck all the entities relevancy and check for new ones
        Collection<EntityRef<E>> currentlyTrackedByRule = new HashSet<>(trackedEntities.get(entityRelevancyRule).getRootEntities());

        final Collection<EntityRef<E>> newlyTrackedByRule = entityRelevancyRule.listDirectlyRelevantEntities(clientEntity);

        for (EntityRef<E> newlyTrackedEntity : newlyTrackedByRule) {
            if (!currentlyTrackedByRule.contains(newlyTrackedEntity)) {
                entitiesToAdd.add(newlyTrackedEntity);
            }
        }

        currentlyTrackedByRule.removeAll(newlyTrackedByRule);

        entityRelevanceRuleUpdated(entityManager, entityRelevancyRule, entitiesToAdd, currentlyTrackedByRule);
    }

    public void entityRelevanceRuleUpdated(EntityManager<E> entityManager, EntityRelevancyRule<E> entityRelevancyRule,
                                           Collection<EntityRef<E>> directEntitiesToAdd, Collection<EntityRef<E>> directEntitiesToRemove) {

        for (EntityRef<E> entity : directEntitiesToRemove) {
            final int entityId = entityManager.getEntityId(entity);
            trackingRules.remove(entity, entityRelevancyRule);
            updateOrRemoveEntity(entity, entityId, trackingRules.get(entity));
        }

        for (EntityRef<E> entity : directEntitiesToAdd) {
            final int entityId = entityManager.getEntityId(entity);
            trackingRules.put(entity, entityRelevancyRule);
            updateEntity(trackingRules.get(entity), entity, entityId);
        }
    }

    public boolean isTrackingEntity(EntityRef<E> entity) {
        return trackingRules.containsKey(entity);
    }

    public EntityRef<E> getClientEntity() {
        return clientEntity;
    }

    public Client<E> getClient() {
        return client;
    }

    public void checkEntityStatusForEntityRelevanceRule(EntityManager<E> entityManager, EntityRelevancyRule<E> entityRelevancyRule,
                                                        EntityRef<E> entity) {
        final ObjectCloud<EntityRef<E>> entityCloud = trackedEntities.get(entityRelevancyRule);

        boolean relevant = entityRelevancyRule.isEntityDirectlyRelevant(clientEntity, entity);
        boolean tracked = entityCloud.isRootEntity(entity);

        if (relevant && !tracked) {
            Set<EntityRef<E>> entitiesToUpdate = new HashSet<>();
            entitiesToUpdate.add(entity);
            processEntityGraph(entityRelevancyRule, entityCloud, true, entity, entitiesToUpdate);

            for (EntityRef<E> entityToUpdate : entitiesToUpdate) {
                updateEntity(trackingRules.get(entityToUpdate), entity, entityManager.getEntityId(entity));
            }
        } else if (!relevant && tracked) {
            Set<EntityRef<E>> entitiesToUpdateOrRemove = new HashSet<>();

            entitiesToUpdateOrRemove.add(entity);
            entitiesToUpdateOrRemove.addAll(entityCloud.setEntityState(false, entity, Collections.<EntityRef<E>>emptySet()));

            for (EntityRef<E> entityToUpdateOrRemove : entitiesToUpdateOrRemove) {
                trackingRules.remove(entityToUpdateOrRemove, entityRelevancyRule);
                updateOrRemoveEntity(entityToUpdateOrRemove, entityManager.getEntityId(entity), trackingRules.get(entity));
            }
        }
    }

    private void processEntityGraph(EntityRelevancyRule<E> entityRelevancyRule,
                                    ObjectCloud<EntityRef<E>> trackedEntities, boolean root, EntityRef<E> entity,
                                    Collection<EntityRef<E>> entitiesToUpdate) {
        trackingRules.put(entity, entityRelevancyRule);
        final Collection<EntityRef<E>> dependentEntities = entityRelevancyRule.listDependentRelevantEntities(clientEntity, entity);
        trackedEntities.setEntityState(root, entity, dependentEntities);

        entitiesToUpdate.addAll(dependentEntities);

        for (EntityRef<E> dependentEntity : dependentEntities) {
            processEntityGraph(entityRelevancyRule, trackedEntities, false, dependentEntity, entitiesToUpdate);
        }
    }

    private void updateOrRemoveEntity(EntityRef<E> entity, int entityId, Collection<EntityRelevancyRule<E>> trackingRules) {
        if (trackingRules.isEmpty()) {
            client.removeEntity(entityId);
        } else {
            updateEntity(trackingRules, entity, entityId);
        }
    }

    private void updateEntity(Collection<EntityRelevancyRule<E>> relevancyRules, EntityRef<E> entity, int entityId) {
        client.updateEntity(entityId, entity, relevancyRules);
    }
}
