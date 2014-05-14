package com.gempukku.secsy.entity.event;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ComponentUpdated extends Event {
    private Class<? extends Component> clazz;

    public ComponentUpdated(Class<? extends Component> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Component> getComponent() {
        return clazz;
    }
}
