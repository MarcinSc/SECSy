package com.gempukku.secsy.entity;

import com.gempukku.secsy.entity.component.InternalComponentManager;
import com.gempukku.secsy.entity.event.Event;
import com.gempukku.secsy.entity.io.ComponentData;

import java.util.*;

public class SimpleEntityRef implements EntityRef {
    private InternalComponentManager internalComponentManager;
    private EntityListener entityListener;
    private EntityEventListener entityEventListener;
    private SimpleEntity entity;

    private Set<Class<? extends Component>> newComponents = new HashSet<Class<? extends Component>>();
    private Map<Class<? extends Component>, Component> usedComponents = new HashMap<Class<? extends Component>, Component>();
    private Set<Class<? extends Component>> removedComponents = new HashSet<Class<? extends Component>>();
    private boolean readOnly;

    public SimpleEntityRef(InternalComponentManager internalComponentManager,
                           EntityListener entityListener,
                           EntityEventListener entityEventListener,
                           SimpleEntity entity, boolean readOnly) {
        this.internalComponentManager = internalComponentManager;
        this.entityListener = entityListener;
        this.entityEventListener = entityEventListener;
        this.entity = entity;
        this.readOnly = readOnly;
    }

    public SimpleEntity getEntity() {
        return entity;
    }

    @Override
    public <T extends Component> T createComponent(Class<T> clazz) {
        validateWritable();
        if (usedComponents.containsKey(clazz) || entity.hasComponent(clazz))
            throw new IllegalStateException("This entity ref already has this component defined");

        T component = internalComponentManager.createComponent(this, clazz);
        newComponents.add(clazz);
        usedComponents.put(clazz, component);

        return component;
    }

    @Override
    public <T extends Component> T getComponent(Class<T> clazz) {
        // First check if this EntityRef already has a component of that class to work with
        Component component = usedComponents.get(clazz);
        if (component != null)
            return (T) component;

        T resultComponent;

        T originalComponent = (T) entity.entityValues.get(clazz);
        if (originalComponent != null) {
            if (readOnly)
                resultComponent = internalComponentManager.copyComponentUnmodifiable(originalComponent, false);
            else
                resultComponent = internalComponentManager.copyComponent(this, originalComponent);
        } else {
            if (entity.template != null && !entity.removedComponents.contains(clazz)) {
                ComponentData componentData = entity.template.getComponentData(clazz);
                if (componentData == null)
                    return null;
                resultComponent = createComponentFromData(componentData, clazz);
            } else
                return null;
        }

        usedComponents.put(clazz, resultComponent);
        return resultComponent;
    }

    @Override
    public void saveChanges() {
        // Validation
        validateWritable();

        for (Class<? extends Component> componentClass : removedComponents) {
            if (!entity.hasComponent(componentClass))
                throw new IllegalStateException("This entity does not contain a component of that class");
        }

        for (Map.Entry<Class<? extends Component>, Component> componentEntry : usedComponents.entrySet()) {
            Class<? extends Component> clazz = componentEntry.getKey();
            if (newComponents.contains(clazz)) {
                if (entity.hasComponent(clazz))
                    throw new IllegalStateException("This entity already contains a component of that class");
            } else {
                if (!entity.hasComponent(clazz))
                    throw new IllegalStateException("This entity does not contain a component of that class");
            }
        }


        // Actual data changing
        for (Class<? extends Component> componentClass : removedComponents) {
            entity.entityValues.remove(componentClass);
            if (entity.template != null && entity.template.hasComponent(componentClass))
                entity.removedComponents.add(componentClass);
            usedComponents.remove(componentClass);
        }

        for (Component component : usedComponents.values()) {
            final Class<Component> clazz = internalComponentManager.getComponentClass(component);
            if (newComponents.contains(clazz)) {
                Component storedComponent = internalComponentManager.copyComponent(null, component);
                entity.entityValues.put(clazz, storedComponent);
                entity.removedComponents.remove(clazz);

                internalComponentManager.saveComponent(storedComponent, component);
            } else {
                Component originalComponent = entity.entityValues.get(clazz);
                if (originalComponent != null)
                    internalComponentManager.saveComponent(originalComponent, component);
                else {
                    Component storedComponent = internalComponentManager.copyComponent(null, component);
                    entity.entityValues.put(clazz, storedComponent);
                    entity.removedComponents.remove(clazz);

                    internalComponentManager.saveComponent(storedComponent, component);
                }
            }
        }

        for (Component usedComponent : usedComponents.values()) {
            internalComponentManager.invalidateComponent(usedComponent);
        }

        removedComponents.clear();
        newComponents.clear();
        usedComponents.clear();

        entityListener.entitiesModified(Collections.singleton(entity));
    }

    @Override
    public void removeComponents(Class<? extends Component>... clazz) {
        validateWritable();
        for (Class<? extends Component> tClass : clazz) {
            removedComponents.add(tClass);
        }
    }

    @Override
    public Iterable<Class<? extends Component>> listComponents() {
        Set<Class<? extends Component>> result = new HashSet<Class<? extends Component>>(entity.entityValues.keySet());
        if (entity.template != null) {
            for (ComponentData componentData : entity.template.getComponentsData()) {
                if (!entity.removedComponents.contains(componentData.getComponentClass()))
                    result.add(componentData.getComponentClass());
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return entity.hasComponent(component);
    }

    @Override
    public boolean exists() {
        return entity.exists;
    }

    @Override
    public void send(Event event) {
        validateWritable();
        entityEventListener.eventSent(this, event);
    }

    private void validateWritable() {
        if (readOnly)
            throw new IllegalStateException("This entity is in read only mode");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleEntityRef that = (SimpleEntityRef) o;

        return entity.equals(that.entity);

    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }

    private <T extends Component> T createComponentFromData(ComponentData componentData, Class<T> componentClass) {
        final T component = internalComponentManager.createComponent(null, componentClass);
        componentData.outputFields(
                new ComponentData.ComponentDataOutput() {
                    @Override
                    public void addField(String field, Object value) {
                        if (value instanceof List) {
                            internalComponentManager.setComponentFieldValue(component, field, new LinkedList((List) value));
                        } else {
                            internalComponentManager.setComponentFieldValue(component, field, value);
                        }
                    }
                }
        );
        return component;
    }
}