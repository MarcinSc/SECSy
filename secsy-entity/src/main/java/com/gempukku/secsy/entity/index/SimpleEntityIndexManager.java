package com.gempukku.secsy.entity.index;

import com.gempukku.secsy.context.annotation.Inject;
import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.context.system.AbstractLifeCycleSystem;
import com.gempukku.secsy.entity.*;

import java.util.HashSet;
import java.util.Set;

@RegisterSystem(
        profiles = "simpleEntityIndexManager",
        shared = EntityIndexManager.class)
public class SimpleEntityIndexManager extends AbstractLifeCycleSystem implements EntityIndexManager, EntityRefCreationCallback, EntityListener {
    @Inject
    private InternalEntityManager internalEntityManager;

    private Set<ComponentEntityIndex> indices = new HashSet<ComponentEntityIndex>();

    @Override
    public void initialize() {
        internalEntityManager.addEntityListener(this);
    }

    @Override
    public EntityIndex addIndexOnComponents(Class<? extends Component>... components) {
        for (ComponentEntityIndex index : indices) {
            Class<? extends Component>[] indexedComponents = index.getIndexedComponents();
            if (indexedComponents.length == components.length) {
                if (hasAll(indexedComponents, components))
                    return index;
            }
        }

        ComponentEntityIndex index = new ComponentEntityIndex(this, components);
        indices.add(index);
        return index;
    }

    private boolean hasAll(Class<? extends Component>[] indexedComponents, Class<? extends Component>[] components) {
        for (Class<? extends Component> indexedComponent : indexedComponents) {
            boolean contains = false;
            for (Class<? extends Component> component : components) {
                if (indexedComponent == component) {
                    contains = true;
                    break;
                }
            }
            if (!contains)
                return false;
        }
        return true;
    }

    @Override
    public EntityRef createEntityRef(SimpleEntity entity) {
        return internalEntityManager.wrapEntityStub(entity);
    }

    @Override
    public void entitiesModified(Iterable<SimpleEntity> entity) {
        for (ComponentEntityIndex index : indices) {
            index.entitiesModified(entity);
        }
    }
}
