package com.gempukku.secsy;

import com.gempukku.secsy.entity.EntityStorage;
import com.gempukku.secsy.entity.event.BeforeComponentDeactivated;
import com.gempukku.secsy.entity.event.ComponentActivated;
import com.gempukku.secsy.entity.io.EntityInputStream;
import com.gempukku.secsy.entity.io.EntityOutputStream;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PersistingEntityManager extends SimpleEntityManager {
    private EventBus<Event> eventBus;
    private EntityStorage<Event> entityStorage;

    public PersistingEntityManager(EventBus<Event> eventBus, EntityFactory<Event> entityFactory, EntityStorage<Event> entityStorage) {
        super(eventBus, entityFactory, entityStorage);
        this.eventBus = eventBus;
        this.entityStorage = entityStorage;
    }

    public void unloadEntity(EntityOutputStream<Event> outputStream, EntityRef<Event> entity) {
        final Collection<Class<? extends Component>> components = entity.listComponents();
        if (components.size() > 0) {
            eventBus.sendEvent(entity, new BeforeComponentDeactivated(components));
        }
        final int entityId = getEntityId(entity);
        outputStream.writeEntity(entityId, entity);
        entityStorage.removeEntity(entity);
    }

    public void loadEntity(EntityInputStream<Event> inputStream) {
        final EntityInputStream.EntityWithId<Event> entityEntry = inputStream.readEntity();
        entityStorage.storeEntityWithId(entityEntry.id, entityEntry.entity);

        final Collection<Class<? extends Component>> components = entityEntry.entity.listComponents();
        if (components.size() > 0) {
            eventBus.sendEvent(entityEntry.entity, new ComponentActivated(components));
        }
    }
}