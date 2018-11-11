package com.gempukku.secsy.entity.index;

import com.gempukku.secsy.entity.Component;
import com.gempukku.secsy.entity.EntityRef;
import com.gempukku.secsy.entity.SimpleEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ComponentEntityIndex implements EntityIndex {
    private Set<SimpleEntity> entitiesInIndex = new HashSet<SimpleEntity>();

    private EntityRefCreationCallback callback;
    private Class<? extends Component>[] indexedComponents;

    public ComponentEntityIndex(EntityRefCreationCallback callback, Class<? extends Component>... indexedComponents) {
        this.callback = callback;
        this.indexedComponents = indexedComponents;
    }

    public Class<? extends Component>[] getIndexedComponents() {
        return indexedComponents;
    }

    public void entitiesModified(Iterable<SimpleEntity> entities) {
        for (SimpleEntity entity : entities) {
            if (entitiesInIndex.contains(entity)) {
                if (!entity.exists) {
                    entitiesInIndex.remove(entity);
                } else if (!hasAllComponents(entity)) {
                    entitiesInIndex.remove(entity);
                }
            } else {
                if (entity.exists && hasAllComponents(entity)) {
                    entitiesInIndex.add(entity);
                }
            }
        }
    }

    @Override
    public Iterable<EntityRef> getEntities() {
        Set<EntityRef> result = new HashSet<EntityRef>();
        for (SimpleEntity simpleEntity : entitiesInIndex) {
            result.add(callback.createEntityRef(simpleEntity));
        }
        return result;
    }

    @Override
    public Iterator<EntityRef> iterator() {
        return getEntities().iterator();
    }

    private boolean hasAllComponents(SimpleEntity entity) {
        for (Class<? extends Component> indexedComponent : indexedComponents) {
            if (!entity.hasComponent(indexedComponent))
                return false;
        }
        return true;
    }
}
