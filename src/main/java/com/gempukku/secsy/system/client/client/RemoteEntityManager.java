package com.gempukku.secsy.system.client.client;

import com.gempukku.secsy.EntityFactory;
import com.gempukku.secsy.EntityManager;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.SimpleEntityManager;
import com.gempukku.secsy.entity.EntityStorage;
import com.gempukku.secsy.entity.NormalStateListener;
import com.gempukku.secsy.entity.event.BeforeComponentRemoved;
import com.gempukku.secsy.system.In;
import com.gempukku.secsy.system.Share;

import java.util.HashMap;
import java.util.Map;

@Share(EntityManager.class)
public class RemoteEntityManager extends SimpleEntityManager implements ClientEventVisitor<Event> {
    @In
    private EventBus<Event> eventBus;
    @In
    private EntityFactory<Event> entityFactory;
    @In
    private EntityStorage<Event> entityStorage;
    @In
    private ClientEventQueue<Event> clientEventQueue;

    private Map<Integer, Integer> remoteEntityToLocalMap = new HashMap<>();
    private Map<Integer, Integer> localEntityToRemoveMap = new HashMap<>();

    public RemoteEntityManager() {
    }

    public RemoteEntityManager(EventBus<Event> eventBus, EntityFactory<Event> entityFactory, EntityStorage<Event> entityStorage,
                               ClientEventQueue<Event> clientEventQueue) {
        super(eventBus, entityFactory, entityStorage);
        this.eventBus = eventBus;
        this.entityFactory = entityFactory;
        this.entityStorage = entityStorage;
        this.clientEventQueue = clientEventQueue;
    }

    public void processAwaitingMessages() {
        clientEventQueue.visitQueuedEvents(this);
    }

    @Override
    public void visitEntityUpdate(int entityId, com.gempukku.secsy.entity.io.EntityState<Event> entityState) {
        Integer localId = remoteEntityToLocalMap.get(entityId);
        EntityRef<Event> entity;
        if (localId == null) {
            entity = entityFactory.createEntity(new NormalStateListener(eventBus));
        } else {
            entity = entityStorage.getEntityById(localId);
        }
        if (localId == null) {
            localId = entityStorage.storeNewEntity(entity);
            remoteEntityToLocalMap.put(entityId, localId);
            localEntityToRemoveMap.put(localId, entityId);
        }

        entityState.applyState(entity);
    }

    @Override
    public void visitEntityRemove(int entityId) {
        final Integer localId = remoteEntityToLocalMap.get(entityId);
        final EntityRef<Event> entity = entityStorage.getEntityById(localId);
        
        eventBus.sendEvent(entity, new BeforeComponentRemoved(entity.listComponents()));

        entityStorage.removeEntity(entity);
        entityFactory.destroyEntity(entity);
        
        remoteEntityToLocalMap.remove(entityId);
        localEntityToRemoveMap.remove(localId);
    }

    @Override
    public void visitEventSend(int entityId, Event event) {
        final Integer localId = remoteEntityToLocalMap.get(entityId);
        final EntityRef<Event> entity = entityStorage.getEntityById(localId);
        eventBus.sendEvent(entity, event);
    }
}
