package com.gempukku.secsy.system.indexing;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.EventListener;
import com.gempukku.secsy.entity.event.BeforeComponentDeactivated;
import com.gempukku.secsy.entity.event.BeforeComponentRemoved;
import com.gempukku.secsy.entity.event.ComponentActivated;
import com.gempukku.secsy.entity.event.ComponentAdded;
import com.gempukku.secsy.entity.event.ComponentEvent;
import com.gempukku.secsy.system.Share;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Share(value = ComponentIndexingManager.class)
public class ComponentIndexingSystem implements ComponentIndexingManager<Event>, EventListener<Event> {
    private Map<Set<Class<? extends Component>>, RealIndex> indices = new HashMap<>();
    private Multimap<RealIndex, IndexAccess> indexAccess = HashMultimap.create();

    private Set<Class<? extends ComponentEvent>> addingEvents = new HashSet<>(Arrays.asList(ComponentAdded.class, ComponentActivated.class));
    private Set<Class<? extends ComponentEvent>> removingEvents = new HashSet<>(Arrays.asList(BeforeComponentRemoved.class, BeforeComponentDeactivated.class));

    private boolean finishedRegistration;

    public void finishIndexRegistration() {
        finishedRegistration = true;
    }

    @Override
    public void eventReceived(EntityRef<Event> entity, Event event) {
        if (!finishedRegistration) {
            throw new IllegalStateException("Should not receive events before system finishes initialization");
        }

        final Class<? extends Object> eventClass = event.getClass();
        if (addingEvents.contains(eventClass)) {
            for (Map.Entry<Set<Class<? extends Component>>, RealIndex> index : indices.entrySet()){
                final Collection<Class<? extends Component>> components = entity.listComponents();
                if (components.containsAll(index.getKey())
                        && containsAny(index.getKey(), ((ComponentEvent) event).getComponents())) {
                    index.getValue().addEntity(entity);
                }
            }
        } else if (removingEvents.contains(eventClass)) {
            for (Map.Entry<Set<Class<? extends Component>>, RealIndex> index : indices.entrySet()){
                final Collection<Class<? extends Component>> components = entity.listComponents();
                if (components.containsAll(index.getKey())
                        && containsAny(index.getKey(), ((ComponentEvent) event).getComponents())) {
                    index.getValue().removeEntity(entity);
                }
            }
        }
    }

    private boolean containsAny(Collection<Class<? extends Component>> collection, Collection<Class<? extends Component>> possibleValues) {
        for (Class<? extends Component> possibleValue : possibleValues) {
            if (collection.contains(possibleValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ComponentIndex<Event> createIndex(Class<? extends Component>... components) {
        if (finishedRegistration) {
            throw new IllegalStateException("Creating new indexes not possible after system finishes initialization");
        }
        final Set<Class<? extends Component>> componentSet = new HashSet<>(Arrays.asList(components));

        RealIndex realIndex = indices.get(componentSet);
        if (realIndex == null) {
            realIndex = new RealIndex(componentSet);
            indices.put(componentSet, realIndex);
        }

        final IndexAccess indexAccess = new IndexAccess(realIndex);
        this.indexAccess.put(realIndex, indexAccess);

        return indexAccess;
    }

    @Override
    public void destroyIndex(ComponentIndex<Event> componentIndex) {
        final IndexAccess access = (IndexAccess) componentIndex;
        access.setDisposed(true);

        final RealIndex realIndex = access.delegate;
        indexAccess.remove(realIndex, componentIndex);
        if (!indexAccess.containsKey(realIndex)) {
            indices.remove(realIndex.componentSet);
        }
    }

    private static class RealIndex implements ComponentIndex<Event> {
        private Set<Class<? extends Component>> componentSet;
        private Set<EntityRef<Event>> matchingEntities = new HashSet<>();

        private RealIndex(Set<Class<? extends Component>> componentSet) {
            this.componentSet = componentSet;
        }

        public void addEntity(EntityRef<Event> entity) {
            matchingEntities.add(entity);
        }

        public void removeEntity(EntityRef<Event> entity) {
            matchingEntities.remove(entity);
        }

        @Override
        public Iterator<EntityRef<Event>> getMatchingEntities() {
            return matchingEntities.iterator();
        }
    }

    private static class IndexAccess implements ComponentIndex<Event> {
        private RealIndex delegate;
        private boolean disposed;

        private IndexAccess(RealIndex delegate) {
            this.delegate = delegate;
        }

        public void setDisposed(boolean disposed) {
            this.disposed = disposed;
        }

        @Override
        public Iterator<EntityRef<Event>> getMatchingEntities() {
            if (disposed) {
                throw new IllegalStateException("This index has been destroyed");
            }
            return delegate.getMatchingEntities();
        }
    }
}
