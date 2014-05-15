package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.entity.event.BeforeComponentRemoved;
import com.gempukku.secsy.entity.event.ComponentAdded;
import com.gempukku.secsy.entity.event.ComponentUpdated;

import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class NormalStateListener implements EntityListener<Event> {
    private EventBus<Event> eventBus;

    public NormalStateListener(EventBus<Event> eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void afterComponentAdded(EntityRef<Event> entity, Set<Class<? extends Component>> components) {
        eventBus.sendEvent(entity, new ComponentAdded(components));
    }

    @Override
    public void afterComponentUpdated(EntityRef<Event> entity, Set<Class<? extends Component>> components) {
        eventBus.sendEvent(entity, new ComponentUpdated(components));
    }

    @Override
    public void beforeComponentRemoved(EntityRef<Event> entity, Set<Class<? extends Component>> components) {
        eventBus.sendEvent(entity, new BeforeComponentRemoved(components));
    }

    @Override
    public void eventSent(EntityRef<Event> entity, Event event) {
        eventBus.sendEvent(entity, event);
    }
}
