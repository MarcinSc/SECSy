package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientSystem<E> {
    private EntityManager<E> entityManager;

    private Map<String, Client<E>> clients = new HashMap<>();
    private Map<String, EntityRef<E>> clientEntities = new HashMap<>();
    // Directly relevant entities and what rules cause them to be tracked
    private Map<String, Multimap<EntityRef<E>, EntityRelevancyRule<E>>> trackedEntitiesOnClient = new HashMap<>();

    private Set<EntityRelevancyRule<E>> entityRelevancyRules = new HashSet<>();
    private Set<EventRelevancyRule<E>> eventRelevancyRules = new HashSet<>();

    public void setEntityManager(EntityManager<E> entityManager) {
        this.entityManager = entityManager;
    }

    public void addClient(String clientId, EntityRef<E> clientEntity, Client<E> client) {
        final Multimap<EntityRef<E>, EntityRelevancyRule<E>> trackedEntities = HashMultimap.create();

        clients.put(clientId, client);
        clientEntities.put(clientId, clientEntity);
        trackedEntitiesOnClient.put(clientId, trackedEntities);

        // Send any entities that are relevant to the client
        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity)) {
                int entityId = entityManager.getEntityId(relevantEntity);
                trackedEntities.put(relevantEntity, entityRelevancyRule);
                updateEntity(trackedEntities.get(relevantEntity), client, relevantEntity, entityId);
            }
        }
    }

    public void removeClient(String clientId) {
        // Just stop sending anything to the client.
        // There is no point removing it from the client, we assume client cleans up itself
        clients.remove(clientId);
        clientEntities.remove(clientId);
        trackedEntitiesOnClient.remove(clientId);
    }

    public void addEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule) {
        entityRelevancyRules.add(entityRelevancyRule);

        // Send any entities that become relevant thanks to this rule
        for (Map.Entry<String, EntityRef<E>> clientEntity : clientEntities.entrySet()) {
            String clientId = clientEntity.getKey();
            final Multimap<Integer, EntityRelevancyRule<E>> trackedEntities = trackedEntitiesOnClient.get(clientId);
            final Client<E> client = clients.get(clientId);
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity.getValue())) {
                int entityId = entityManager.getEntityId(relevantEntity);
                trackedEntities.put(entityId, entityRelevancyRule);
                updateEntity(trackedEntities.get(entityId), client, relevantEntity, entityId);
            }
        }
    }

    public void removeEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule) {
        entityRelevancyRules.remove(entityRelevancyRule);

        // Remove entities that are relevant thanks to this rule from the client,
        // unless they are relevant thanks to any other rule
        for (Map.Entry<String, EntityRef<E>> clientIdAndEntity : clientEntities.entrySet()) {
            String clientId = clientIdAndEntity.getKey();
            final Client<E> client = clients.get(clientId);
            final EntityRef<E> clientEntity = clientIdAndEntity.getValue();
            final Multimap<Integer, EntityRelevancyRule<E>> trackedEntities = trackedEntitiesOnClient.get(clientId);

            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity)) {
                final int entityId = entityManager.getEntityId(relevantEntity);
                trackedEntities.remove(entityId, entityRelevancyRule);
                updateOrRemoveEntity(relevantEntity, entityId, trackedEntities.get(entityId), client);
            }
        }
    }

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule) {
        Set<EntityRef<E>> entitiesToAdd = new HashSet<>();
        Set<EntityRef<E>> entitiesToRemove = new HashSet<>();

        // Recheck all the entities relevancy and check for new ones
        Set<Integer> currentlyTrackedByRule = new HashSet<>();
        final Multimap<Integer, EntityRelevancyRule<E>> relevantEntities = trackedEntitiesOnClient.get(clientId);
        for (Map.Entry<Integer, Collection<EntityRelevancyRule<E>>> entityEntry : relevantEntities.asMap().entrySet()){
            if (entityEntry.getValue().contains(entityRelevancyRule)) {
                currentlyTrackedByRule.add(entityEntry.getKey());
            }
        }

        Set<Integer> newlyTrackedEntityIds = new HashSet<>();
        for (EntityRef<E> newlyTrackedEntity : entityRelevancyRule.listRelevantEntities(clientEntities.get(clientId))) {
            final int entityId = entityManager.getEntityId(newlyTrackedEntity);
            newlyTrackedEntityIds.add(entityId);
            if (!currentlyTrackedByRule.contains(entityId)) {
                entitiesToAdd.add(newlyTrackedEntity);
            }
        }

        currentlyTrackedByRule.removeAll(newlyTrackedEntityIds);

        for (Integer entityIdToRemove : currentlyTrackedByRule) {
            entitiesToRemove.add(entityManager.getEntityById(entityIdToRemove));
        }

        entityRelevanceRuleUpdatedForClient(clientId, entityRelevancyRule, entitiesToAdd, entitiesToRemove);
    }

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule,
                                                    Collection<EntityRef<E>> entitiesToAdd, Collection<EntityRef<E>> entitiesToRemove) {

        final Multimap<Integer, EntityRelevancyRule<E>> trackedEntities = trackedEntitiesOnClient.get(clientId);
        final Client<E> client = clients.get(clientId);

        for (EntityRef<E> entity : entitiesToRemove) {
            final int entityId = entityManager.getEntityId(entity);
            trackedEntities.remove(entityId, entityRelevancyRule);
            updateOrRemoveEntity(entity, entityId, trackedEntities.get(entityId), client);
        }

        for (EntityRef<E> entity : entitiesToAdd) {
            final int entityId = entityManager.getEntityId(entity);
            trackedEntities.put(entityId, entityRelevancyRule);
            updateEntity(trackedEntities.get(entityId), client, entity, entityId);
        }
    }

    public void addEventRelevancyRule(EventRelevancyRule<E> eventRelevancyRule) {
        eventRelevancyRules.add(eventRelevancyRule);
    }

    public void removeEventRelevancyRule(EventRelevancyRule<E> eventRelevancyRule) {
        eventRelevancyRules.remove(eventRelevancyRule);
    }

    public void eventReceived(EntityRef<E> entity, E event) {
        final int entityId = entityManager.getEntityId(entity);

        // Send the event to any clients tracking that entity if it is relevant for those client
        for (Map.Entry<String, Multimap<Integer, EntityRelevancyRule<E>>> clientTracked : trackedEntitiesOnClient.entrySet()){
            if (clientTracked.getValue().containsKey(entityId)) {
                final String clientId = clientTracked.getKey();
                final EntityRef<E> clientEntity = clientEntities.get(clientId);
                final Client<E> client = clients.get(clientId);

                for (EventRelevancyRule<E> eventRelevancyRule : eventRelevancyRules) {
                    if (eventRelevancyRule.isEventRelevant(clientEntity, entity, event)) {
                        client.sendEvent(entityId, entity, event);
                        break;
                    }
                }
            }
        }

        // If this event impacts any relevance rule, recheck if this entity changes its relevance status for that rule
        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            if (entityRelevancyRule.isRelevanceImpactingEvent(event)) {
                for (Map.Entry<String, EntityRef<E>> clientIdAndEntity : clientEntities.entrySet()) {
                    String clientId = clientIdAndEntity.getKey();
                    final Multimap<Integer, EntityRelevancyRule<E>> trackedEntities = trackedEntitiesOnClient.get(clientId);
                    final Collection<EntityRef<E>> entitiesToTrack = entityRelevancyRule.listEntitiesToTrackDueToImpactingEvent(clientIdAndEntity.getValue(), entity);
                    final Client<E> client = clients.get(clientId);

                    boolean tracked = trackedEntities.get(entityId).contains(entityRelevancyRule);
                    if (entitiesToTrack.size() > 0 && !tracked) {
                        trackedEntities.put(entityId, entityRelevancyRule);
                        updateEntity(trackedEntities.get(entityId), client, entity, entityId);
                    } else if (!relevant && tracked) {
                        trackedEntities.remove(entityId, entityRelevancyRule);
                        updateOrRemoveEntity(entity, entityId, trackedEntities.get(entityId), client);
                    }
                }
            }
        }
    }

    private void updateOrRemoveEntity(EntityRef<E> entity, int entityId, Collection<EntityRelevancyRule<E>> trackingRules, Client<E> client) {
        if (trackingRules.isEmpty()) {
            client.removeEntity(entityId);
        } else {
            updateEntity(trackingRules, client, entity, entityId);
        }
    }

    private void updateEntity(Collection<EntityRelevancyRule<E>> relevancyRules, Client<E> client, EntityRef<E> entity, int entityId) {
        client.updateEntity(entityId, entity, relevancyRules);
    }
}
