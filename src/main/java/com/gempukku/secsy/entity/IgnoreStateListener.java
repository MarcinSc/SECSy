package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;

import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class IgnoreStateListener implements EntityListener<Event> {
    @Override
    public void afterComponentAdded(EntityRef<Event> entity, Set<Class<? extends Component>> components) {
    }

    @Override
    public void afterComponentUpdated(EntityRef<Event> entity, Set<Class<? extends Component>> components) {
    }

    @Override
    public void beforeComponentRemoved(EntityRef<Event> entity, Set<Class<? extends Component>> components) {
    }

    @Override
    public void eventSent(EntityRef<Event> entity, Event event) {
    }
}
