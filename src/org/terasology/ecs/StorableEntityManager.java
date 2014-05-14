package org.terasology.ecs;

import org.terasology.ecs.event.Event;
import org.terasology.ecs.storage.EntityOutputStream;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class StorableEntityManager implements EntityManager<Event> {
    private EventBus<Event> eventBus;
    private EntityListener<Event> normalStateListener;

    public StorableEntityManager(EventBus<Event> eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public EntityRef<Event> create() {
        return null;
    }

    @Override
    public void destroy(EntityRef<Event> entity) {
    }

    @Override
    public int getEntityId(EntityRef<Event> entity) {
        return 0;
    }

    public void unloadEntity(EntityOutputStream outputStream, EntityRef<Event> entity) {

    }
}
