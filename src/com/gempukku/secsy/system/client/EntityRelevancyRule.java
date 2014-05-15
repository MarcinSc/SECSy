package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

import java.util.Set;

public interface EntityRelevancyRule<E> {
    public Set<EntityRef<E>> getRelevantEntities();

}
