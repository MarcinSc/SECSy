package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityFactory;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.component.ComponentFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class IsolationEntityFactory<E> implements EntityFactory<E> {
    private ComponentFactory<Object> componentFactory;

    public IsolationEntityFactory(ComponentFactory componentFactory) {
        this.componentFactory = (ComponentFactory<Object>) componentFactory;
    }

    public void setEntityListener(EntityRef<E> entity, EntityListener<E> entityListener) {
        ((EntityRefImpl) entity).setEntityListener(entityListener);
    }

    @Override
    public EntityRef<E> createEntity(EntityListener<E> entityListener) {
        return new EntityRefImpl(entityListener);
    }

    @Override
    public void destroyEntity(EntityRef<E> entity) {
        ((EntityRefImpl) entity).exists = false;
    }

    private class EntityRefImpl implements EntityRef<E> {
        private Set<Class<? extends Component>> storedComponents = new HashSet<>();
        private Map<Class<? extends Component>, Object> componentValueObjects = new HashMap<>();
        private EntityListener<E> entityListener;
        private boolean exists = true;

        private EntityRefImpl(EntityListener<E> entityListener) {
            this.entityListener = entityListener;
        }

        private void setEntityListener(EntityListener<E> entityListener) {
            this.entityListener = entityListener;
        }

        @Override
        public synchronized <T extends Component> T addComponent(Class<T> clazz) {
            if (!componentValueObjects.containsKey(clazz)) {
                componentValueObjects.put(clazz, componentFactory.createComponentValueObject(clazz));
            }
            return componentFactory.createComponent(clazz, componentValueObjects.get(clazz));
        }

        @Override
        public synchronized <T extends Component> T getComponent(Class<T> clazz) {
            if (!storedComponents.contains(clazz)) {
                return null;
            }
            return componentFactory.getComponent(clazz, componentValueObjects.get(clazz));
        }

        @Override
        public synchronized void saveComponents(Component... components) {
            for (Component component : components) {
                Class<? extends Component> clazz = component.getComponentClass();
                if (componentFactory.isNewComponent(component)) {
                    if (storedComponents.contains(clazz)) {
                        throw new IllegalStateException("This entity already contains a component of that class");
                    }
                } else {
                    if (!storedComponents.contains(clazz)) {
                        throw new IllegalStateException("This entity does not contain a component of that class");
                    }
                }
            }

            Set<Class<? extends Component>> addedComponents = new HashSet<>();

            for (Component component : components) {
                if (componentFactory.isNewComponent(component)) {
                    Class<? extends Component> clazz = component.getComponentClass();
                    final Object valueObject = componentValueObjects.get(clazz);
                    componentFactory.saveComponent(component, valueObject);

                    storedComponents.add(clazz);
                    addedComponents.add(clazz);
                }
            }

            if (addedComponents.size() > 0) {
                entityListener.afterComponentAdded(this, Collections.unmodifiableSet(addedComponents));
            }

            Set<Class<? extends Component>> updatedComponents = new HashSet<>();

            for (Component component : components) {
                if (!componentFactory.isNewComponent(component)) {
                    Class<? extends Component> clazz = component.getComponentClass();
                    final Object valueObject = componentValueObjects.get(clazz);
                    componentFactory.saveComponent(component, valueObject);

                    updatedComponents.add(clazz);
                }
            }

            if (updatedComponents.size() > 0) {
                entityListener.afterComponentUpdated(this, Collections.unmodifiableSet(updatedComponents));
            }
        }

        @Override
        public synchronized <T extends Component> void removeComponents(Class<T>... clazz) {
            Set<Class<? extends Component>> removedComponents = new HashSet<>();

            for (Class<T> componentClass : clazz) {
                if (!storedComponents.contains(componentClass)) {
                    throw new IllegalStateException("This entity does not contain a component of that class");
                }
                removedComponents.add(componentClass);
            }

            entityListener.beforeComponentRemoved(this, Collections.unmodifiableSet(removedComponents));
            for (Class<T> componentClass : clazz) {
                storedComponents.remove(componentClass);
                final Object valueObject = componentValueObjects.remove(componentClass);
                componentFactory.disposeComponentValueObject(valueObject);
            }
        }

        @Override
        public Collection<Class<? extends Component>> listComponents() {
            return Collections.unmodifiableCollection(storedComponents);
        }

        @Override
        public boolean exists() {
            return exists;
        }

        @Override
        public void send(E event) {
            entityListener.eventSent(this, event);
        }
    }
}

