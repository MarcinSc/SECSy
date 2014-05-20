package com.gempukku.secsy.system.client.host;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;

public interface EntityComponentFieldFilter<E> {
    public boolean isComponentRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Component component);
    public boolean isComponentFieldRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Component component, String field);
}
