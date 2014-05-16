package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ClientSystem<E> {
    private EntityManager<E> entityManager;

    private Map<String, Client<E>> clients = new HashMap<>();
    private Map<String, EntityRef<E>> clientEntities = new HashMap<>();
    private Map<String, Multimap<EntityRef<E>, EntityRelevancyRule<E>>> rulesTrackingEntities = new HashMap<>();
    private Map<String, Map<EntityRelevancyRule<E>, EntityCloud<E>>> trackedEntityClouds = new HashMap<>();

    private Set<EntityRelevancyRule<E>> entityRelevancyRules = new HashSet<>();
    private Set<EventRelevancyRule<E>> eventRelevancyRules = new HashSet<>();

    public void setEntityManager(EntityManager<E> entityManager) {
        this.entityManager = entityManager;
    }

    public void addClient(String clientId, EntityRef<E> clientEntity, Client<E> client) {
        final Multimap<EntityRef<E>, EntityRelevancyRule<E>> trackingRules = HashMultimap.create();

        clients.put(clientId, client);
        clientEntities.put(clientId, clientEntity);
        rulesTrackingEntities.put(clientId, trackingRules);

        Map<EntityRelevancyRule<E>, EntityCloud<E>> trackedEntities = new HashMap<>();
        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            trackedEntities.put(entityRelevancyRule, new SimpleEntityCloud<E>());
        }
        trackedEntityClouds.put(clientId, trackedEntities);

        processNewRelevancyRulesForClient(entityRelevancyRules, clientEntity, client, trackingRules, trackedEntities);
    }

    private void processNewRelevancyRulesForClient(Set<EntityRelevancyRule<E>> relevancyRules, EntityRef<E> clientEntity, Client<E> client, Multimap<EntityRef<E>, EntityRelevancyRule<E>> trackingRules, Map<EntityRelevancyRule<E>, EntityCloud<E>> trackedEntities) {
        Set<EntityRef<E>> entitiesToUpdate = new HashSet<>();

        // Send any entities that are relevant to the client
        for (EntityRelevancyRule<E> entityRelevancyRule : relevancyRules) {
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listDirectlyRelevantEntities(clientEntity)) {
                entitiesToUpdate.add(relevantEntity);
                processEntityGraph(entityRelevancyRule, trackingRules, trackedEntities.get(entityRelevancyRule), true, clientEntity, relevantEntity, entitiesToUpdate);
            }
        }

        for (EntityRef<E> entity : entitiesToUpdate) {
            updateEntity(trackingRules.get(entity), client, entity, entityManager.getEntityId(entity));
        }
    }

    public void removeClient(String clientId) {
        // Just stop sending anything to the client.
        // There is no point removing it from the client, we assume client cleans up itself
        clients.remove(clientId);
        clientEntities.remove(clientId);
        rulesTrackingEntities.remove(clientId);
        trackedEntityClouds.remove(clientId);
    }

    public void addEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule) {
        entityRelevancyRules.add(entityRelevancyRule);

        // Send any entities that become relevant thanks to this rule
        for (Map.Entry<String, EntityRef<E>> clientIdAndEntity : clientEntities.entrySet()) {
            String clientId = clientIdAndEntity.getKey();

            final Multimap<EntityRef<E>, EntityRelevancyRule<E>> trackingRules = rulesTrackingEntities.get(clientId);
            final Map<EntityRelevancyRule<E>, EntityCloud<E>> trackedEntities = trackedEntityClouds.get(clientId);
            trackedEntities.put(entityRelevancyRule, new SimpleEntityCloud<E>());
            
            final EntityRef<E> clientEntity = clientEntities.get(clientId);
            final Client<E> client = clients.get(clientId);

            processNewRelevancyRulesForClient(Collections.singleton(entityRelevancyRule), clientEntity, client,
                    trackingRules, trackedEntities);
        }
    }

    public void removeEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule) {
        entityRelevancyRules.remove(entityRelevancyRule);

        // Remove entities that are relevant thanks to this rule from the client,
        // unless they are relevant thanks to any other rule
        for (Map.Entry<String, EntityRef<E>> clientIdAndEntity : clientEntities.entrySet()) {
            String clientId = clientIdAndEntity.getKey();
            final Client<E> client = clients.get(clientId);
            final Multimap<EntityRef<E>, EntityRelevancyRule<E>> trackingRules = rulesTrackingEntities.get(clientId);
            final EntityCloud<E> trackedEntities = trackedEntityClouds.get(clientId).remove(entityRelevancyRule);

            final Collection<EntityRef<E>> changedEntities = trackedEntities.getAllEntities();

            for (EntityRef<E> entity : changedEntities) {
                trackingRules.remove(entity, entityRelevancyRule);
            }

            for (EntityRef<E> relevantEntity : changedEntities) {
                final int entityId = entityManager.getEntityId(relevantEntity);
                updateOrRemoveEntity(relevantEntity, entityId, trackingRules.get(relevantEntity), client);
            }
        }
    }

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule) {
        Set<EntityRef<E>> entitiesToAdd = new HashSet<>();

        // Recheck all the entities relevancy and check for new ones
        Collection<EntityRef<E>> currentlyTrackedByRule = new HashSet<>(trackedEntityClouds.get(clientId).get(entityRelevancyRule).getRootEntities());

        final Collection<EntityRef<E>> newlyTrackedByRule = entityRelevancyRule.listDirectlyRelevantEntities(clientEntities.get(clientId));

        for (EntityRef<E> newlyTrackedEntity : newlyTrackedByRule) {
            if (!currentlyTrackedByRule.contains(newlyTrackedEntity)) {
                entitiesToAdd.add(newlyTrackedEntity);
            }
        }

        currentlyTrackedByRule.removeAll(newlyTrackedByRule);

        entityRelevanceRuleUpdatedForClient(clientId, entityRelevancyRule, entitiesToAdd, currentlyTrackedByRule);
    }

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule,
                                                    Collection<EntityRef<E>> directEntitiesToAdd, Collection<EntityRef<E>> directEntitiesToRemove) {

        final Multimap<Integer, EntityRelevancyRule<E>> trackedEntities = rulesTrackingEntities.get(clientId);
        final Client<E> client = clients.get(clientId);

        for (EntityRef<E> entity : directEntitiesToRemove) {
            final int entityId = entityManager.getEntityId(entity);
            trackedEntities.remove(entityId, entityRelevancyRule);
            updateOrRemoveEntity(entity, entityId, trackedEntities.get(entityId), client);
        }

        for (EntityRef<E> entity : directEntitiesToAdd) {
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
        for (Map.Entry<String, Multimap<Integer, EntityRelevancyRule<E>>> clientTracked : rulesTrackingEntities.entrySet()) {
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
                    final Multimap<Integer, EntityRelevancyRule<E>> trackedEntities = rulesTrackingEntities.get(clientId);
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

    private void processEntityGraph(EntityRelevancyRule<E> entityRelevancyRule,
                                    Multimap<EntityRef<E>, EntityRelevancyRule<E>> trackingRules,
                                    EntityCloud<E> trackedEntities, boolean root, EntityRef<E> clientEntity, EntityRef<E> entity,
                                    Collection<EntityRef<E>> entitiesToUpdate) {
        trackingRules.put(entity, entityRelevancyRule);
        final Collection<EntityRef<E>> dependentEntities = entityRelevancyRule.listDependentRelevantEntities(clientEntity, entity);
        trackedEntities.setEntityState(root, entity, dependentEntities);

        entitiesToUpdate.addAll(dependentEntities);

        for (EntityRef<E> dependentEntity : dependentEntities) {
            processEntityGraph(entityRelevancyRule, trackingRules, trackedEntities, false, clientEntity, dependentEntity, entitiesToUpdate);
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
