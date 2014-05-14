package com.gempukku.secsy;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EntityListener<E> {
    public void afterComponentAdded(EntityRef<E> entity, Class<? extends Component> component);
    public void afterComponentUpdated(EntityRef<E> entity, Class<? extends Component> component);
    public void beforeComponentRemoved(EntityRef<E> entity, Class<? extends Component> ... component);
    public void eventSent(EntityRef<E> entity, E event);
}
