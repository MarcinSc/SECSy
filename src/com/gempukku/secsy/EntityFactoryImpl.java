package com.gempukku.secsy;

import com.gempukku.secsy.component.Component;
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
public class EntityFactoryImpl<E> implements EntityFactory<E> {
    private ComponentFactory<Object> componentFactory;

    public EntityFactoryImpl(ComponentFactory componentFactory) {
        this.componentFactory = (ComponentFactory<Object>) componentFactory;
    }

    public void setEntityListener(EntityRef<E> entity, EntityListener<E> entityListener) {
        ((EntityRefImpl) entity).setEntityListener(entityListener);
    }

    @Override
    public EntityRef<E> createEntity() {
        return new EntityRefImpl();
    }

    @Override
    public void destroyEntity(EntityRef<E> entity) {
    }

    private class EntityRefImpl implements EntityRef<E> {
        private Set<Class<? extends Component>> storedComponents = new HashSet<>();
        private Map<Class<? extends Component>, Object> componentValueObjects = new HashMap<>();
        private EntityListener<E> entityListener;

        private void setEntityListener(EntityListener<E> entityListener) {
            this.entityListener = entityListener;
        }

        @Override
        public synchronized <T extends Component> T addComponent(Class<T> clazz) {
            if (!componentValueObjects.containsKey(clazz)) {
                componentValueObjects.put(clazz, componentFactory.createComponentValueObject(clazz));
            }
            return (T) componentFactory.createComponent(clazz, componentValueObjects.get(clazz));
        }

        @Override
        public synchronized <T extends Component> T getComponent(Class<T> clazz) {
            if (!storedComponents.contains(clazz)) {
                throw new IllegalStateException("This entity does not contain a component of that class");
            }
            return (T) componentFactory.getComponent(clazz, componentValueObjects.get(clazz));
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

            for (Component component : components) {
                Class<? extends Component> clazz = component.getComponentClass();
                final Object valueObject = componentValueObjects.get(clazz);
                componentFactory.saveComponent(component, valueObject);

                if (componentFactory.isNewComponent(component)) {
                    storedComponents.add(clazz);
                    if (entityListener != null) {
                        entityListener.afterComponentAdded(this, clazz);
                    }
                } else {
                    if (entityListener != null) {
                        entityListener.afterComponentUpdated(this, clazz);
                    }
                }
            }
        }

        @Override
        public synchronized <T extends Component> void removeComponents(Class<T>... clazz) {
            for (Class<T> componentClass : clazz) {
                if (!storedComponents.contains(componentClass)) {
                    throw new IllegalStateException("This entity does not contain a component of that class");
                }
            }

            if (entityListener != null) {
                entityListener.beforeComponentRemoved(this, clazz);
            }
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
            return true;
        }

        @Override
        public void send(E event) {
            if (entityListener != null) {
                entityListener.eventSent(this, event);
            }
        }
    }
}

