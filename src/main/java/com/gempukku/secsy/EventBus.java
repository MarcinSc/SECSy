package com.gempukku.secsy;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface EventBus<E> {
    public void sendEvent(EntityRef<E> entity, E event);
    public void addEventListener(EventListener<E> eventListener);
    public void removeEventListener(EventListener<E> eventListener);
}
