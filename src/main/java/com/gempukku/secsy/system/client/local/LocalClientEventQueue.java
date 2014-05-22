package com.gempukku.secsy.system.client.local;

import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.EventListener;
import com.gempukku.secsy.bus.SimpleEventBus;
import com.gempukku.secsy.entity.io.EntitySerializer;
import com.gempukku.secsy.event.EventSerializer;
import com.gempukku.secsy.system.In;
import com.gempukku.secsy.system.Share;
import com.gempukku.secsy.system.client.ContextEventFilter;
import com.gempukku.secsy.system.client.client.ClientEventQueue;
import com.gempukku.secsy.system.client.host.Client;
import com.gempukku.secsy.system.client.host.ClientCallback;
import com.gempukku.secsy.system.client.host.CompositeEntityComponentFieldFilter;
import com.gempukku.secsy.system.client.host.EntityComponentFieldFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Share(ClientEventQueue.class)
public class LocalClientEventQueue<E> implements ClientEventQueue<E>, Client<E>, EventBus<E> {
    @In
    private EntitySerializer<E> entitySerializer;
    @In
    private EventSerializer<E> eventSerializer;
    @In
    private ContextEventFilter<E> contextEventFilter;

    private EventBus<E> eventBus = new SimpleEventBus<>();
    private Queue<ClientEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private List<ClientEvent> updateQueue = new LinkedList<>();
    private ClientCallback<E> clientCallback;

    public LocalClientEventQueue() {
    }

    public LocalClientEventQueue(EntitySerializer<E> entitySerializer, EventSerializer<E> eventSerializer) {
        this.entitySerializer = entitySerializer;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public void addEventListener(EventListener<E> eventListener) {
        eventBus.addEventListener(eventListener);
    }

    @Override
    public void removeEventListener(EventListener<E> eventListener) {
        eventBus.removeEventListener(eventListener);
    }

    @Override
    public void sendEvent(EntityRef<E> entity, E event) {
        eventBus.sendEvent(entity, event);
    }

    @Override
    public void sendServerEvent(int entityId, E event) {
        if (contextEventFilter.isToServerEvent(event)) {
            clientCallback.sendEvent(entityId, event);
        }
    }

    @Override
    public void setClientCallback(ClientCallback<E> clientCallback) {
        this.clientCallback = new LocalClientCallback<>(clientCallback, eventSerializer);
    }

    @Override
    public void updateEntity(int entityId, EntityRef<E> entity, Collection<? extends EntityComponentFieldFilter<E>> entityComponentFieldFilters) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        entitySerializer.serializeEntity(entity, os, new CompositeEntityComponentFieldFilter<>(entityComponentFieldFilters));

        updateQueue.add(new ClientEvent(ClientEventType.UPDATE, entityId, os.toByteArray()));
    }

    @Override
    public void removeEntity(int entityId) {
        updateQueue.add(new ClientEvent(ClientEventType.REMOVE, entityId, null));
    }

    @Override
    public void sendEvent(int entityId, EntityRef<E> entity, E event) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        eventSerializer.serializeEvent(event, os);

        updateQueue.add(new ClientEvent(ClientEventType.EVENT, entityId, os.toByteArray()));
    }

    @Override
    public void visitQueuedEvents(ClientEventVisitor<E> visitor) {
        while (true) {
            final ClientEvent clientEvent = eventQueue.poll();
            if (clientEvent != null) {
                clientEvent.processVisitor(visitor);
            } else {
                break;
            }
        }
    }

    @Override
    public void commitChanges() {
        eventQueue.addAll(updateQueue);
        updateQueue.clear();
    }

    public enum ClientEventType {
        UPDATE, REMOVE, EVENT
    }

    private class ClientEvent {
        private byte[] binaryData;
        private int entityId;

        private ClientEventType type;

        private ClientEvent(ClientEventType type, int entityId, byte[] binaryData) {
            this.type = type;
            this.entityId = entityId;
            this.binaryData = binaryData;
        }

        private void processVisitor(ClientEventVisitor<E> visitor) {
            if (type == ClientEventType.UPDATE) {
                visitor.visitEntityUpdate(entityId, entitySerializer.deserializeEntity(new ByteArrayInputStream(binaryData)));
            } else if (type == ClientEventType.REMOVE) {
                visitor.visitEntityRemove(entityId);
            } else {
                visitor.visitEventSend(entityId, eventSerializer.deserializeEvent(new ByteArrayInputStream(binaryData)));
            }
        }
    }
}
