package com.gempukku.secsy.entity.event;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.Event;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BeforeComponentRemoved extends Event {
    private Collection<Class<? extends Component>> components;

    public BeforeComponentRemoved(Collection<Class<? extends Component>> components) {
        this.components = components;
    }

    public Collection<Class<? extends Component>> getComponents() {
        return Collections.unmodifiableCollection(components);
    }
}
