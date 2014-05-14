package org.terasology.ecs;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class EntityFactoryImpl<E> implements EntityFactory<E> {
    private ComponentFactory componentFactory;

    public EntityFactoryImpl(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
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
        private Set<Class<? extends Component>> storedComponents = Sets.newHashSet();
        private Table<Class<? extends Component>, String, Object> savedValues = HashBasedTable.create();
        private EntityListener<E> entityListener;

        private void setEntityListener(EntityListener<E> entityListener) {
            this.entityListener = entityListener;
        }

        @Override
        public synchronized <T extends Component> T addComponent(Class<T> clazz) {
            if (storedComponents.contains(clazz)) {
                throw new IllegalStateException("This entity already contains a component of that class");
            }
            return componentFactory.createComponent(clazz);
        }

        @Override
        public synchronized <T extends Component> T getComponent(Class<T> clazz) {
            if (!storedComponents.contains(clazz)) {
                throw new IllegalStateException("This entity does not contain a component of that class");
            }
            return componentFactory.getComponent(clazz, savedValues.row(clazz));
        }

        @Override
        public synchronized void saveComponents(Component ... components) {
            for (Component component : components) {
                Class<? extends Component> clazz = component.getClass();
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
                Class<? extends Component> clazz = component.getClass();
                if (componentFactory.isNewComponent(component)) {
                    storedComponents.add(clazz);
                    savedValues.rowMap().put(clazz, componentFactory.saveComponent(component));
                    if (entityListener != null) {
                        entityListener.afterComponentAdded(this, clazz);
                    }
                } else {
                    savedValues.rowMap().put(clazz, componentFactory.saveComponent(component));
                    if (entityListener != null) {
                        entityListener.afterComponentUpdated(this, clazz);
                    }
                }
            }
        }

        @Override
        public synchronized <T extends Component> void removeComponents(Class<T> ... clazz) {
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
                savedValues.rowMap().remove(componentClass);
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

