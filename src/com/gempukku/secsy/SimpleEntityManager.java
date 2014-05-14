package com.gempukku.secsy;

import com.gempukku.secsy.entity.EntityListener;
import com.gempukku.secsy.entity.EntityStorage;
import com.gempukku.secsy.entity.NormalStateListener;
import com.gempukku.secsy.event.BeforeComponentRemoved;
import com.gempukku.secsy.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleEntityManager implements EntityManager<Event> {
    private EventBus<Event> eventBus;
    private EntityFactory<Event> entityFactory;
    private EntityStorage<Event> entityStorage;
    private EntityListener<Event> normalStateListener;

    public SimpleEntityManager(EventBus<Event> eventBus, EntityFactory<Event> entityFactory, EntityStorage<Event> entityStorage) {
        this.eventBus = eventBus;
        this.entityFactory = entityFactory;
        this.entityStorage = entityStorage;
        normalStateListener = new NormalStateListener(eventBus);
    }

    @Override
    public EntityRef<Event> create() {
        EntityRef<Event> entity = entityFactory.createEntity(normalStateListener);
        entityStorage.storeNewEntity(entity);
        entityFactory.setEntityListener(entity, normalStateListener);
        return entity;
    }

    @Override
    public void destroy(EntityRef<Event> entity) {
        eventBus.sendEvent(entity, new BeforeComponentRemoved(entity.listComponents()));
        entityStorage.removeEntity(entity);
        entityFactory.destroyEntity(entity);
    }

    @Override
    public int getEntityId(EntityRef<Event> entity) {
        return entityStorage.getEntityId(entity);
    }
}
