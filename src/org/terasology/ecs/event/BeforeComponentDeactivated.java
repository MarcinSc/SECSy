package org.terasology.ecs.event;

import org.terasology.ecs.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BeforeComponentDeactivated extends Event {
    private Collection<Class<? extends Component>> components;

    public BeforeComponentDeactivated(Collection<Class<? extends Component>> components) {
        this.components = components;
    }

    public Collection<Class<? extends Component>> getComponents() {
        return Collections.unmodifiableCollection(components);
    }
}
