package com.gempukku.secsy.entity.serialization;

import com.gempukku.secsy.entity.Component;
import com.gempukku.secsy.entity.io.ComponentData;
import com.gempukku.secsy.entity.io.EntityData;
import com.gempukku.secsy.entity.io.StoredEntityData;

import java.util.LinkedList;
import java.util.List;

public class EntityInformation implements StoredEntityData {
    private int entityId = 0;
    private List<ComponentInformation> components = new LinkedList<ComponentInformation>();

    public EntityInformation() {
    }

    public EntityInformation(EntityData toCopy) {
        for (ComponentData componentData : toCopy.getComponentsData()) {
            addComponent(new ComponentInformation(componentData));
        }
    }


    public Iterable<ComponentInformation> getComponentsData() {
        return components;
    }

    @Override
    public ComponentData getComponentData(Class<? extends Component> componentClass) {
        for (ComponentInformation component : components) {
            if (component.getComponentClass() == componentClass)
                return component;
        }
        return null;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> componentClass) {
        for (ComponentInformation component : components) {
            if (component.getComponentClass() == componentClass)
                return true;
        }
        return false;
    }

    public void addComponent(ComponentInformation componentInformation) {
        components.add(componentInformation);
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
}
