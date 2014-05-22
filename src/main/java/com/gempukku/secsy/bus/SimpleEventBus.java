package com.gempukku.secsy.bus;

import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.EventListener;
import com.gempukku.secsy.system.Share;

import java.util.LinkedList;
import java.util.List;

@Share(EventBus.class)
public class SimpleEventBus<E> implements EventBus<E> {
    private List<EventListener<E>> eventListeners = new LinkedList<>();

    @Override
    public void addEventListener(EventListener<E> eventListener) {
        eventListeners.add(eventListener);
    }

    @Override
    public void removeEventListener(EventListener<E> eventListener) {
        eventListeners.remove(eventListener);
    }

    @Override
    public void sendEvent(EntityRef<E> entity, E event) {
        for (EventListener<E> eventListener : eventListeners) {
            eventListener.eventReceived(entity, event);
        }
    }
}
