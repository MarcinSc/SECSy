package com.gempukku.secsy.system.client;

import com.gempukku.secsy.EntityRef;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SimpleEntityCloud<E> implements EntityCloud<E> {
    private BiDiMultimap<EntityRef<E>, EntityRef<E>> dependencies = new BiDiMultimap<>();
    private Set<EntityRef<E>> roots = new HashSet<>();

    @Override
    public boolean containsEntity(EntityRef<E> entity) {
        return roots.contains(entity) || dependencies.containsValue(entity);
    }

    @Override
    public Collection<EntityRef<E>> setEntityState(boolean root, EntityRef<E> entity, Collection<EntityRef<E>> entityDependencies) {
        if (roots.contains(entity)) {
            if (!root) {
                roots.remove(entity);
            }
        } else {
            if (root) {
                roots.add(entity);
            } else {
                if (!dependencies.containsValue(entity)) {
                    throw new IllegalArgumentException("Trying to add dependency for non-descendant non-root entity");
                }
            }
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

        // Finally if it is no longer referenced and not a root, remove it as well
        if (!root && !dependencies.containsValue(entity)) {
            removedEntities.add(entity);
        }

        return removedEntities;
    }

    @Override
    public Collection<EntityRef<E>> getRootEntities() {
        return Collections.unmodifiableSet(roots);
    }

    @Override
    public boolean isRootEntity(EntityRef<E> entity) {
        return roots.contains(entity);
    }

    @Override
    public Collection<EntityRef<E>> getAllEntities() {
        Set<EntityRef<E>> result = new HashSet<>();
        result.addAll(roots);
        result.addAll(dependencies.getAllValues());
        return result;
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
}
