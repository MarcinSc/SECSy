package com.gempukku.secsy;

import com.gempukku.secsy.entity.EntityListener;
import com.gempukku.secsy.entity.EntityStorage;
import com.gempukku.secsy.entity.NormalStateListener;
import com.gempukku.secsy.entity.event.BeforeComponentRemoved;
import com.gempukku.secsy.system.DefaultLifeCycleSystem;
import com.gempukku.secsy.system.In;
import com.gempukku.secsy.system.LifeCycleSystem;
import com.gempukku.secsy.system.Share;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@Share(EntityManager.class)
public class SimpleEntityManager extends DefaultLifeCycleSystem implements EntityManager<Event> {
    @In
    private EventBus<Event> eventBus;
    @In
    private EntityFactory<Event> entityFactory;
    @In
    private EntityStorage<Event> entityStorage;
    private EntityListener<Event> normalStateListener;

    public SimpleEntityManager() {
    }

    public SimpleEntityManager(EventBus<Event> eventBus, EntityFactory<Event> entityFactory, EntityStorage<Event> entityStorage) {
        this.entityFactory = entityFactory;
        this.entityStorage = entityStorage;
        this.eventBus = eventBus;
    }

    @Override
    public void postInitialize() {
        normalStateListener = new NormalStateListener(eventBus);
    }

    @Override
    public EntityRef<Event> create() {
        EntityRef<Event> entity = entityFactory.createEntity(normalStateListener);
        entityStorage.storeNewEntity(entity);
        return entity;
    }

    @Override
    public void destroy(EntityRef<Event> entity) {
        final Collection<Class<? extends Component>> components = entity.listComponents();
        if (components.size() > 0) {
            eventBus.sendEvent(entity, new BeforeComponentRemoved(components));
        }
        entityStorage.removeEntity(entity);
        entityFactory.destroyEntity(entity);
    }

    @Override
    public int getEntityId(EntityRef<Event> entity) {
        return entityStorage.getEntityId(entity);
    }

    @Override
    public EntityRef<Event> getEntityById(int entityId) {
        return entityStorage.getEntityById(entityId);
    }
}
