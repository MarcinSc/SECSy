package com.gempukku.secsy.system;

import java.util.Collection;

public interface SystemInitializer<S> {
    public void initializeSystems(Collection<S> systems);
}
