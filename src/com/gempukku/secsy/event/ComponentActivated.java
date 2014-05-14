package com.gempukku.secsy.event;

import com.gempukku.secsy.component.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ComponentActivated extends Event {
    private Collection<Class<? extends Component>> components;

    public ComponentActivated(Collection<Class<? extends Component>> components) {
        this.components = components;
    }

    public Collection<Class<? extends Component>> getComponents() {
        return Collections.unmodifiableCollection(components);
    }
}
