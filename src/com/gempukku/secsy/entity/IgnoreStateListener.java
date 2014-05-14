package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class IgnoreStateListener implements EntityListener<Event> {
    @Override
    public void afterComponentAdded(EntityRef<Event> entity, Class<? extends Component> component) {
    }

    @Override
    public void afterComponentUpdated(EntityRef<Event> entity, Class<? extends Component> component) {
    }

    @Override
    public void beforeComponentRemoved(EntityRef<Event> entity, Class<? extends Component>... component) {
    }

    @Override
    public void eventSent(EntityRef<Event> entity, Event event) {
    }
}
