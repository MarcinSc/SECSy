package com.gempukku.secsy.system.client.host;

import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.EventListener;
import com.gempukku.secsy.system.DefaultLifeCycleSystem;
import com.gempukku.secsy.system.In;
import com.gempukku.secsy.system.Share;
import com.gempukku.secsy.system.client.ContextEventFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

@Share(ClientManager.class)
public class ClientSystem<E> extends DefaultLifeCycleSystem implements EventListener<E>, ClientManager<E>, ClientCallback<E> {
    @In
    private EntityManager<E> entityManager;
    @In
    private ContextEventFilter<E> contextEventFilter;

    private Collection<Class<? extends E>> internalEntityStateEvents;

    private Map<String, ClientConnection<E>> clientConnections = new HashMap<>();

    private Set<EntityRelevancyRule<E>> entityRelevancyRules = new HashSet<>();
    private Set<EventRelevancyRule<E>> eventRelevancyRules = new HashSet<>();

    private Queue<EntityEvent> clientIncomingEvents = new ConcurrentLinkedQueue<>();

    public void setEntityManager(EntityManager<E> entityManager) {
        this.entityManager = entityManager;
    }

    public void setInternalEntityStateEvents(Collection<Class<? extends E>> internalEntityStateEvents) {
        this.internalEntityStateEvents = internalEntityStateEvents;
    }

    public void setContextEventFilter(ContextEventFilter<E> contextEventFilter) {
        this.contextEventFilter = contextEventFilter;
    }

    public ClientCallback<E> addClient(String clientId, EntityRef<E> clientEntity, Client<E> client) {
        final ClientConnection<E> clientConnection = new ClientConnection<E>(client, clientEntity);
        clientConnection.addEntityRelevancyRules(entityManager, entityRelevancyRules);

        clientConnections.put(clientId, clientConnection);

        return this;
    }

    @Override
    public void sendEvent(int entityId, E event) {
        clientIncomingEvents.add(new EntityEvent(entityId, event));
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

    @Override
    public void update(long delta) {
        while (true) {
        final EntityEvent event = clientIncomingEvents.poll();
            if (event == null) {
                break;
            }
            final EntityRef<E> entity = entityManager.getEntityById(event.entityId);
            if (entity != null) {
                entity.send(event.event);
            }
        }
    }

    @Override
    public void postUpdate() {
        commitClientChanges();
    }

    public void commitClientChanges() {
        for (ClientConnection<E> clientConnection : clientConnections.values()) {
            clientConnection.getClient().commitChanges();
        }
    }

    @Override
    public void eventReceived(EntityRef<E> entity, E event) {
        if (isInternalEntityStateEvent(event)) {
            for (ClientConnection<E> clientConnection : clientConnections.values()) {
                if (clientConnection.isTrackingEntity(entity)) {
                    clientConnection.entityStateChanged(entityManager, entity);
                }
            }
        } else if (contextEventFilter.isToClientEvent(event)) {
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

    private boolean isInternalEntityStateEvent(E event) {
        return internalEntityStateEvents.contains(event.getClass());
    }

    private class EntityEvent {
        private int entityId;
        private E event;

        private EntityEvent(int entityId, E event) {
            this.entityId = entityId;
            this.event = event;
        }
    }
}
