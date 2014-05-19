package com.gempukku.secsy;

public interface EventListener<E> {
    public void eventReceived(EntityRef<E> entity, E event);
}
