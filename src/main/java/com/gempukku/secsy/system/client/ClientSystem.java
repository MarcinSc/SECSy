package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientSystem<E> {
    private EntityManager<E> entityManager;

    private Map<String, ClientConnection<E>> clientConnections = new HashMap<>();

    private Set<EntityRelevancyRule<E>> entityRelevancyRules = new HashSet<>();
    private Set<EventRelevancyRule<E>> eventRelevancyRules = new HashSet<>();

    public void setEntityManager(EntityManager<E> entityManager) {
        this.entityManager = entityManager;
    }

    public void addClient(String clientId, EntityRef<E> clientEntity, Client<E> client) {
        final ClientConnection<E> clientConnection = new ClientConnection<E>(client, clientEntity);
        clientConnection.addEntityRelevancyRules(entityManager, entityRelevancyRules);

        clientConnections.put(clientId, clientConnection);
    }

    public void removeClient(String clientId) {
        // Just stop sending anything to the client.
        // There is no point removing it from the client, we assume client cleans up itself
        clientConnections.remove(clientId);
    }

    public void addEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule) {
        entityRelevancyRules.add(entityRelevancyRule);

        for (ClientConnection<E> clientConnection : clientConnections.values()) {
            clientConnection.addEntityRelevancyRules(entityManager, Collections.singleton(entityRelevancyRule));
        }
    }

    public void removeEntityRelevancyRule(EntityRelevancyRule<E> entityRelevancyRule) {
        entityRelevancyRules.remove(entityRelevancyRule);

        for (ClientConnection<E> clientConnection : clientConnections.values()) {
            clientConnection.removeEntityRelevancyRules(entityManager, entityRelevancyRule);
        }
    }

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule) {
        clientConnections.get(clientId).entityRelevanceRuleUpdated(entityManager, entityRelevancyRule);
    }

    public void entityRelevanceRuleUpdatedForClient(String clientId, EntityRelevancyRule<E> entityRelevancyRule,
                                                    Collection<EntityRef<E>> directEntitiesToAdd, Collection<EntityRef<E>> directEntitiesToRemove) {
        clientConnections.get(clientId).entityRelevanceRuleUpdated(entityManager, entityRelevancyRule, directEntitiesToAdd, directEntitiesToRemove);
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
        for (ClientConnection<E> clientConnection : clientConnections.values()) {
            if (clientConnection.isTrackingEntity(entity)) {
                final EntityRef<E> clientEntity = clientConnection.getClientEntity();
                final Client<E> client = clientConnection.getClient();
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
                for (ClientConnection<E> clientConnection : clientConnections.values()) {
                    clientConnection.checkEntityStatusForEntityRelevanceRule(entityManager, entityRelevancyRule, entity);
                }
            }
        }
    }
}
