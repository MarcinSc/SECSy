package com.gempukku.secsy.system;

import java.util.Collection;

public interface SystemInitializer<S> {
    public SystemContext initializeSystems(Collection<S> systems);
    public void destroySystems(SystemContext systemContext, Collection<S> systems);
}
