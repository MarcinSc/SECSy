package com.gempukku.secsy.event;

import com.gempukku.secsy.component.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ComponentAdded extends Event {
    private Class<? extends Component> clazz;

    public ComponentAdded(Class<? extends Component> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Component> getClazz() {
        return clazz;
    }
}
