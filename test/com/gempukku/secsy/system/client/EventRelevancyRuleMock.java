package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

import java.util.HashMap;
import java.util.Map;

public class EventRelevancyRuleMock<E> implements EventRelevancyRule<E> {
    private Map<EntityRef<E>, Map<EntityRef<E>, Class<? extends E>>> relevantEventsForClient = new HashMap<>();

    public void setRelevantClientEntityEvents(EntityRef<E> clientEntity, Map<EntityRef<E>, Class<? extends E>> eventClasses) {
        relevantEventsForClient.put(clientEntity, eventClasses);
    }

    @Override
    public boolean isEventRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, E event) {
        final Map<EntityRef<E>, Class<? extends E>> relevantEvents = relevantEventsForClient.get(clientEntity);
        if (relevantEvents == null) {
            return false;
        }
        final Class<? extends E> acceptedClass = relevantEvents.get(entity);
        return acceptedClass != null && acceptedClass == event.getClass();
    }
}
