package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityListener<E> {
    public void afterComponentAdded(EntityRef<E> entity, Class<? extends Component> component);

    public void afterComponentUpdated(EntityRef<E> entity, Class<? extends Component> component);

    public void beforeComponentRemoved(EntityRef<E> entity, Class<? extends Component>... component);

    public void eventSent(EntityRef<E> entity, E event);
}
