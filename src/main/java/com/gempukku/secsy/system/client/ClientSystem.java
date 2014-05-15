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

        // Send any entities that are relevant to the client
        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity)) {
                int entityId = entityManager.getEntityId(relevantEntity);
                addEntityIfNotTracked(trackedEntities, client, relevantEntity, entityId);
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
            final Set<Integer> trackedEntities = trackedEntitiesOnClient.get(clientId);
            final Client<E> client = clients.get(clientId);
            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity.getValue())) {
                int entityId = entityManager.getEntityId(relevantEntity);
                addEntityIfNotTracked(trackedEntities, client, relevantEntity, entityId);
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

            for (EntityRef<E> relevantEntity : entityRelevancyRule.listRelevantEntities(clientEntity)) {
                removeIfNoLongerRelevant(client, clientEntity, relevantEntity, entityManager.getEntityId(relevantEntity));
            }
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

        // If this event impacts any relevance rule, recheck all the tracked entities for that rule unless they
        // are relevant for any other rule
        for (EntityRelevancyRule<E> entityRelevancyRule : entityRelevancyRules) {
            if (entityRelevancyRule.isRelevanceImpactingEvent(event)) {
                for (Map.Entry<String, EntityRef<E>> clientIdAndEntity : clientEntities.entrySet()) {
                    String clientId = clientIdAndEntity.getKey();
                    final Set<Integer> trackedEntities = trackedEntitiesOnClient.get(clientId);

                    final boolean relevant = entityRelevancyRule.isEntityRelevant(clientIdAndEntity.getValue(), entity);

                    if (relevant) {
                        addEntityIfNotTracked(trackedEntities, clients.get(clientId), entity, entityId);
                    } else {
                        removeIfNoLongerRelevant(clients.get(clientId), clientIdAndEntity.getValue(), entity, entityId);
                    }
                }
            }
        }
    }

    private void addEntityIfNotTracked(Set<Integer> trackedEntities, Client<E> client, EntityRef<E> relevantEntity, int entityId) {
        if (!trackedEntities.contains(entityId)) {
            client.updateEntity(entityId, relevantEntity);
            trackedEntities.add(entityId);
        }
    }

    private void removeIfNoLongerRelevant(Client<E> client, EntityRef<E> clientEntity, EntityRef<E> relevantEntity, int entityId) {
        if (!isStillRelevant(clientEntity, relevantEntity)) {
            client.removeEntity(entityId);
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
}
