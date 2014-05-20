package com.gempukku.secsy.system.client.host;

import com.gempukku.secsy.EntityRef;

public interface EventRelevancyRule<E> {
    public boolean isEventRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, E event);
}
