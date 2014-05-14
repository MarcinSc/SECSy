package com.gempukku.secsy.event;

import com.gempukku.secsy.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ComponentUpdated extends Event {
    private Class<? extends Component> clazz;

    public ComponentUpdated(Class<? extends Component> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Component> getClazz() {
        return clazz;
    }
}
