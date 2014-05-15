package com.gempukku.secsy.entity.event;

import com.gempukku.secsy.Component;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BeforeComponentDeactivated extends ComponentEvent {
    public BeforeComponentDeactivated(Collection<Class<? extends Component>> components) {
        super(components);
    }
}
