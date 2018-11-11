package com.gempukku.secsy.entity;

import com.gempukku.secsy.entity.component.InternalComponentManager;
import com.gempukku.secsy.entity.io.ComponentData;
import com.gempukku.secsy.entity.io.EntityData;
import com.gempukku.secsy.entity.io.StoredEntityData;

import java.util.*;

public class SimpleEntity implements StoredEntityData {
    private InternalComponentManager internalComponentManager;
    public final int id;
    public Map<Class<? extends Component>, Component> entityValues = new HashMap<Class<? extends Component>, Component>();
    public Set<Class<? extends Component>> removedComponents = new HashSet<Class<? extends Component>>();
    public EntityData template;
    public boolean exists = true;

    public SimpleEntity(InternalComponentManager internalComponentManager, int id, EntityData template) {
        this.template = template;
        this.internalComponentManager = internalComponentManager;
        this.id = id;
    }

    public SimpleEntity(InternalComponentManager internalComponentManager, int id) {
        this(internalComponentManager, id, null);
    }

    @Override
    public int getEntityId() {
        return id;
    }

    @Override
    public Iterable<ComponentData> getComponentsData() {
        List<ComponentData> result = new LinkedList<ComponentData>();
        if (template != null) {
            for (ComponentData componentDataFromTemplate : template.getComponentsData()) {
                if (!removedComponents.contains(componentDataFromTemplate.getComponentClass())
                        && !entityValues.containsKey(componentDataFromTemplate.getComponentClass()))
                    result.add(componentDataFromTemplate);
            }
        }
        for (Map.Entry<Class<? extends Component>, Component> entry : entityValues.entrySet()) {
            result.add(convertToComponentData(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    @Override
    public ComponentData getComponentData(Class<? extends Component> componentClass) {
        Component component = entityValues.get(componentClass);
        if (component == null) {
            if (!removedComponents.contains(componentClass))
                return template.getComponentData(componentClass);
            return null;
        }
        return convertToComponentData(componentClass, component);
    }

    @Override
    public boolean hasComponent(Class<? extends Component> componentClass) {
        return entityValues.containsKey(componentClass)
                || (template != null && !removedComponents.contains(componentClass) && template.hasComponent(componentClass));
    }

    private ComponentData convertToComponentData(final Class<? extends Component> componentClass, final Component component) {
        return new ComponentData() {
            @Override
            public Class<? extends Component> getComponentClass() {
                return componentClass;
            }

            @Override
            public void outputFields(ComponentDataOutput output) {
                for (Map.Entry<String, Class<?>> fieldDef : internalComponentManager.getComponentFieldTypes(component).entrySet()) {
                    String fieldName = fieldDef.getKey();
                    Object fieldValue = internalComponentManager.getComponentFieldValue(component, fieldName, componentClass);
                    output.addField(fieldName, fieldValue);
                }
            }
        };
    }
}
