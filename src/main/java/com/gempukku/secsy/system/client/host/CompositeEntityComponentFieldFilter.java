package com.gempukku.secsy.system.client.host;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;

import java.util.Collection;

public class CompositeEntityComponentFieldFilter<E> implements EntityComponentFieldFilter<E> {
    private Collection<? extends EntityComponentFieldFilter<E>> filters;

    public CompositeEntityComponentFieldFilter(Collection<? extends EntityComponentFieldFilter<E>> filters) {
        this.filters = filters;
    }

    @Override
    public boolean isComponentRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Class<? extends Component> component) {
        for (EntityComponentFieldFilter<E> filter : filters) {
            if (filter.isComponentRelevant(clientEntity, entity, component)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isComponentFieldRelevant(EntityRef<E> clientEntity, EntityRef<E> entity, Class<? extends Component> component, String field) {
        for (EntityComponentFieldFilter<E> filter : filters) {
            if (filter.isComponentFieldRelevant(clientEntity, entity, component, field)) {
                return true;
            }
        }

        return false;
    }
}
