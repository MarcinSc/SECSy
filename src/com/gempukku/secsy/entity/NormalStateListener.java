package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.event.BeforeComponentRemoved;
import com.gempukku.secsy.event.ComponentAdded;
import com.gempukku.secsy.event.ComponentUpdated;
import com.gempukku.secsy.event.Event;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class NormalStateListener implements EntityListener<Event> {
    private EventBus<Event> eventBus;

    public NormalStateListener(EventBus<Event> eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void afterComponentAdded(EntityRef<Event> entity, Class<? extends Component> component) {
        eventBus.sendEvent(entity, new ComponentAdded(component));
    }

    @Override
    public void afterComponentUpdated(EntityRef<Event> entity, Class<? extends Component> component) {
        eventBus.sendEvent(entity, new ComponentUpdated(component));
    }

    @Override
    public void beforeComponentRemoved(EntityRef<Event> entity, Class<? extends Component>... component) {
        eventBus.sendEvent(entity, new BeforeComponentRemoved(Arrays.asList(component)));
    }

    @Override
    public void eventSent(EntityRef<Event> entity, Event event) {
        eventBus.sendEvent(entity, event);
    }
}
