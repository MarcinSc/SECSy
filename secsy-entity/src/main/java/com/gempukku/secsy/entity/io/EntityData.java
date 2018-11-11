package com.gempukku.secsy.entity.io;

import com.gempukku.secsy.entity.Component;

public interface EntityData {
    Iterable<? extends ComponentData> getComponentsData();

    ComponentData getComponentData(Class<? extends Component> componentClass);

    boolean hasComponent(Class<? extends Component> componentClass);
}
