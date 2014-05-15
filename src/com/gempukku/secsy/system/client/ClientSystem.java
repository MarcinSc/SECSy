package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientSystem<E> {
    private EntityManager<E> entityManager;

    private Map<String, Client<E>> clients = new HashMap<>();
    private Map<String, EntityRef<E>> clientEntities = new HashMap<>();
    private Map<String, Set<Integer>> trackedEntitiesOnClient = new HashMap<>();

    private Set<EntityRelevancyRule<E>> entityRelevancyRules = new HashSet<>();
    private Set<EventRelevancyRule<E>> eventRelevancyRules = new HashSet<>();

    public void setEntityManager(EntityManager<E> entityManager) {
        this.entityManager = entityManager;
    }

    public void addClient(String clientId, EntityRef<E> clientEntity, Client<E> client) {
        final HashSet<Integer> trackedEntities = new HashSet<>();

        clients.put(clientId, client);
        clientEntities.put(clientId, clientEntity);
        trackedEntitiesOnClient.put(clientId, trackedEntities);

        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity)) {
                int entityId = entityManager.getEntityId(relevantEntity);
                if (!trackedEntities.contains(entityId)) {
                    client.addEntity(entityId, relevantEntity);
                    trackedEntities.add(entityId);
                }
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

        for (Map.Entry<String, EntityRef<E>> clientEntity : clientEntities.entrySet()) {
            String clientId = clientEntity.getKey();
            final Set<Integer> trackedEntities = trackedEntitiesOnClient.get(clientId);
            final Client<E> client = clients.get(clientId);
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity.getValue())) {
                int entityId = entityManager.getEntityId(relevantEntity);
                if (!trackedEntities.contains(entityId)) {
                    client.addEntity(entityId, relevantEntity);
                    trackedEntities.add(entityId);
                }
            }
        }
    }

    public void removeEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule) {
        entityRelevancyRules.remove(entityRelevancyRule);

        for (Map.Entry<String, EntityRef<E>> clientIdAndEntity : clientEntities.entrySet()) {
            String clientId = clientIdAndEntity.getKey();
            final Client<E> client = clients.get(clientId);
            final EntityRef<E> clientEntity = clientIdAndEntity.getValue();

            // We have to remove any entity that was relevant thanks to this rule from the client,
            // unless it is relevant thanks to any other rule
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity)) {
                if (!isStillRelevant(clientEntity, relevantEntity)) {
                    final int entityId = entityManager.getEntityId(relevantEntity);
                    client.removeEntity(entityId);
                }
            }
        }
    }

    private boolean isStillRelevant(EntityRef<E> clientEntity, EntityRef<E> relevantEntity) {
        boolean relevant = false;
        for (EntityRelevancyRule<E> relevancyRule : entityRelevancyRules) {
            if (relevancyRule.isEntityRelevant(clientEntity, relevantEntity)) {
                relevant = true;
                break;
            }
        }
        return relevant;
    }

    public void addEventRelevancyRule(EventRelevancyRule<E> eventRelevancyRule) {
        eventRelevancyRules.add(eventRelevancyRule);
    }

    public void removeEventRelevancyRule(EventRelevancyRule<E> eventRelevancyRule) {
        eventRelevancyRules.remove(eventRelevancyRule);
    }

    public void eventReceived(EntityRef<E> entity, E event) {
        final int entityId = entityManager.getEntityId(entity);
        for (Map.Entry<String, Set<Integer>> clientTracked : trackedEntitiesOnClient.entrySet()) {
            if (clientTracked.getValue().contains(entityId)) {
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

        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            if (entityRelevancyRule.isRelevanceImpactingEvent(event)) {
                for (Map.Entry<String, EntityRef<E>> clientIdAndEntity : clientEntities.entrySet()) {
                    String clientId = clientIdAndEntity.getKey();
                    final Set<Integer> trackedEntities = trackedEntitiesOnClient.get(clientId);

                    final boolean relevant = entityRelevancyRule.isEntityRelevant(clientIdAndEntity.getValue(), entity);
                    final boolean tracked = trackedEntities.contains(entityId);
                    if (relevant != tracked) {
                        if (tracked) {
                            clients.get(clientId).removeEntity(entityId);
                        } else {
                            clients.get(clientId).addEntity(entityId, entity);
                        }
                    }
                }
            }
        }
    }
}
