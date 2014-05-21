package com.gempukku.secsy.system;

import java.util.Collection;

public interface SystemProducer<E> {
    public Collection<E> createSystems();
}
