package com.gempukku.secsy.entity.event;

import com.gempukku.secsy.entity.Component;

import java.util.Collection;
import java.util.Collections;

public class AfterEntityLoaded extends Event implements ComponentEvent {
    private Collection<Class<? extends Component>> components;

    public AfterEntityLoaded(Collection<Class<? extends Component>> components) {
        this.components = components;
    }

    @Override
    public Collection<Class<? extends Component>> getComponents() {
        return Collections.unmodifiableCollection(components);
    }
}
