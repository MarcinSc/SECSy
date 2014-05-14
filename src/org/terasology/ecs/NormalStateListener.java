package org.terasology.ecs;

import org.terasology.ecs.event.BeforeComponentRemoved;
import org.terasology.ecs.event.ComponentAdded;
import org.terasology.ecs.event.ComponentUpdated;
import org.terasology.ecs.event.Event;

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
