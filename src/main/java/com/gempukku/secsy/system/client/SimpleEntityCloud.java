package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleEntityCloud<E> implements EntityCloud<E> {
    private BiDiMultimap<EntityRef<E>, EntityRef<E>> dependencies = new BiDiMultimap<>();
    private Set<EntityRef<E>> roots = new HashSet<>();

    @Override
    public boolean containsEntity(EntityRef<E> entity) {
        return dependencies.containsKey(entity) || dependencies.containsValue(entity);
    }

    @Override
    public Collection<EntityRef<E>> setEntityState(boolean root, EntityRef<E> entity, Collection<EntityRef<E>> entityDependencies) {
        if (root) {
            roots.add(entity);
        } else {
            if (!dependencies.containsValue(entity)) {
                throw new IllegalArgumentException("Trying to add dependency for non-descendant non-root entity");
            }
            roots.remove(entity);
        }
        Set<EntityRef<E>> removedEntities = new HashSet<>();
        final Collection<EntityRef<E>> oldDependencies = new HashSet<>(dependencies.getValues(entity));

        // Depth first removal
        Set<EntityRef<E>> removedDependencies = new HashSet<>();

        for (EntityRef<E> oldDependency : oldDependencies) {
            if (!entityDependencies.contains(oldDependency)) {
                dependencies.remove(entity, oldDependency);
                removedDependencies.add(oldDependency);
            }
        }

        for (EntityRef<E> newEntity : entityDependencies) {
            if (!oldDependencies.contains(newEntity)) {
                dependencies.put(entity, newEntity);
            }
        }

        processRemovingDependencies(removedEntities, removedDependencies);

        return removedEntities;
    }

    private void processRemovingDependencies(Set<EntityRef<E>> reportedRemovedEntities, Collection<EntityRef<E>> removedDependencies) {
        Set<EntityRef<E>> removedDependenciesForLevel = new HashSet<>();

        for (EntityRef<E> removedDependency : removedDependencies) {
            if (!dependencies.containsValue(removedDependency) && !roots.contains(removedDependency)) {
                removedDependenciesForLevel.addAll(dependencies.removeAllValues(removedDependency));
                reportedRemovedEntities.add(removedDependency);
            }
        }

        if (removedDependenciesForLevel.size() > 0) {
            processRemovingDependencies(reportedRemovedEntities, removedDependenciesForLevel);
        }
    }

    @Override
    public Collection<EntityRef<E>> removeEntity(EntityRef<E> entity) {
        Set<EntityRef<E>> removedEntities = new HashSet<>();
        roots.remove(entity);
        
        final Collection<EntityRef<E>> oldDependencies = dependencies.removeAllValues(entity);
        removedEntities.add(entity);
        processRemovingDependencies(removedEntities, oldDependencies);

        return removedEntities;
    }
}
