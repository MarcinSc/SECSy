package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;

import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityListener<E> {
    public void afterComponentAdded(EntityRef<E> entity, Set<Class<? extends Component>> components);

    public void afterComponentUpdated(EntityRef<E> entity, Set<Class<? extends Component>> components);

    public void beforeComponentRemoved(EntityRef<E> entity, Set<Class<? extends Component>> components);

    public void eventSent(EntityRef<E> entity, E event);
}
