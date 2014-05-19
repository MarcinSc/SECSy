package com.gempukku.secsy.system.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SimpleObjectCloud<E> implements ObjectCloud<E> {
    private BiDiMultimap<E, E> dependencies = new BiDiMultimap<>();
    private Set<E> roots = new HashSet<>();

    @Override
    public boolean containsEntity(E entity) {
        return roots.contains(entity) || dependencies.containsValue(entity);
    }

    @Override
    public Collection<E> setEntityState(boolean root, E entity, Collection<? extends E> entityDependencies) {
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

        Set<E> removedEntities = new HashSet<>();
        final Collection<E> oldDependencies = new HashSet<>(dependencies.getValues(entity));

        // Depth first removal
        Set<E> removedDependencies = new HashSet<>();

        for (E oldDependency : oldDependencies) {
            if (!entityDependencies.contains(oldDependency)) {
                dependencies.remove(entity, oldDependency);
                removedDependencies.add(oldDependency);
            }
        }

        for (E newEntity : entityDependencies) {
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
    public Collection<E> getRootEntities() {
        return Collections.unmodifiableSet(roots);
    }

    @Override
    public boolean isRootEntity(E entity) {
        return roots.contains(entity);
    }

    @Override
    public Collection<E> getAllEntities() {
        Set<E> result = new HashSet<>();
        result.addAll(roots);
        result.addAll(dependencies.getAllValues());
        return result;
    }

    private void processRemovingDependencies(Set<E> reportedRemovedEntities, Collection<E> removedDependencies) {
        Set<E> removedDependenciesForLevel = new HashSet<>();

        for (E removedDependency : removedDependencies) {
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
