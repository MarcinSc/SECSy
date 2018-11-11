package com.gempukku.secsy.entity;

import com.gempukku.secsy.context.annotation.Inject;
import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.context.system.AbstractLifeCycleSystem;
import com.gempukku.secsy.context.util.PriorityCollection;
import com.gempukku.secsy.entity.component.ComponentManager;
import com.gempukku.secsy.entity.component.InternalComponentManager;
import com.gempukku.secsy.entity.event.Event;
import com.gempukku.secsy.entity.game.InternalGameLoop;
import com.gempukku.secsy.entity.game.InternalGameLoopListener;
import com.gempukku.secsy.entity.io.ComponentData;
import com.gempukku.secsy.entity.io.EntityData;
import com.gempukku.secsy.entity.io.StoredEntityData;
import com.gempukku.secsy.entity.prefab.PrefabManager;
import com.gempukku.secsy.entity.relevance.EntityRelevanceRule;
import com.gempukku.secsy.entity.relevance.EntityRelevanceRuleRegistry;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;
import java.util.*;

@RegisterSystem(profiles = "simpleEntityManager", shared = {EntityManager.class, InternalEntityManager.class, EntityRelevanceRuleRegistry.class})
public class SimpleEntityManager extends AbstractLifeCycleSystem implements EntityManager, InternalEntityManager,
        EntityRelevanceRuleRegistry, InternalGameLoopListener {
    @Inject
    private ComponentManager componentManager;
    @Inject
    private InternalComponentManager internalComponentManager;
    @Inject
    private InternalGameLoop internalGameLoop;
    @Inject(optional = true)
    private PrefabManager prefabManager;

    private Set<EntityRelevanceRule> entityRelevanceRules = new HashSet<EntityRelevanceRule>();

    private int maxId;
    private Set<SimpleEntity> entities = new HashSet<SimpleEntity>();

    private DispatchEntityListener dispatchEntityListener = new DispatchEntityListener();
    private DispatchEntityEventListener dispatchEntityEventListener = new DispatchEntityEventListener();

    @Override
    public void addEntityEventListener(EntityEventListener entityEventListener) {
        dispatchEntityEventListener.entityEventListeners.add(entityEventListener);
    }

    @Override
    public void removeEntityEventListener(EntityEventListener entityEventListener) {
        dispatchEntityEventListener.entityEventListeners.remove(entityEventListener);
    }

    @Override
    public void addEntityListener(EntityListener entityListener) {
        dispatchEntityListener.entityListeners.add(entityListener);
    }

    @Override
    public void removeEntityListener(EntityListener entityListener) {
        dispatchEntityListener.entityListeners.remove(entityListener);
    }

    @Override
    public void registerEntityRelevanceRule(EntityRelevanceRule entityRelevanceRule) {
        entityRelevanceRules.add(entityRelevanceRule);
    }

    @Override
    public void deregisterEntityRelevanceRule(EntityRelevanceRule entityRelevanceRule) {
        entityRelevanceRules.remove(entityRelevanceRule);
    }

    @Override
    public void initialize() {
        internalGameLoop.addInternalGameLoopListener(this);
    }

    @Override
    public void destroy() {
        internalGameLoop.removeInternalGameLooplListener(this);
    }

    @Override
    public void preUpdate() {
        // Do nothing
    }

    /**
     * This method unloads all irrelevant entities
     */
    @Override
    public void postUpdate() {
        // First go through all the registered rules and tell them to update
        // their internal rules
        for (EntityRelevanceRule entityRelevanceRule : entityRelevanceRules) {
            entityRelevanceRule.determineRelevance();
        }

        // Determine, which entities are unloaded for which rule
        Multimap<EntityRelevanceRule, SimpleEntity> entitiesToUnloadByRules = determineEntitiesToUnloadByRules();

        // Pass the entities to their rules to store them before unload
        tellRulesToStoreUnloadingEntities(entitiesToUnloadByRules);

        // Send events to them
        Collection<SimpleEntity> entitiesToUnload = entitiesToUnloadByRules.values();

        // Unload the entities
        unloadTheEntities(entitiesToUnload);

        dispatchEntityListener.entitiesModified(entitiesToUnload);

        int lastMaxId = maxId;

        // Load any new entities that became relevant
        Set<SimpleEntity> loadedEntities = loadNewlyRelevantEntities();

        dispatchEntityListener.entitiesModified(loadedEntities);

        for (EntityRelevanceRule entityRelevanceRule : entityRelevanceRules) {
            entityRelevanceRule.newRelevantEntitiesLoaded();
        }
    }

    @Override
    public int getEntityId(EntityRef entityRef) {
        return ((SimpleEntityRef) entityRef).getEntity().getEntityId();
    }

    @Override
    public String getEntityUniqueIdentifier(EntityRef entityRef) {
        return String.valueOf(getEntityId(entityRef));
    }

    private Set<SimpleEntity> loadNewlyRelevantEntities() {
        Set<SimpleEntity> loadedEntities = new HashSet<SimpleEntity>();
        for (EntityRelevanceRule entityRelevanceRule : entityRelevanceRules) {
            for (StoredEntityData entityData : entityRelevanceRule.getNewRelevantEntities()) {
                int id = entityData.getEntityId();
                if (id == 0)
                    id = ++maxId;
                SimpleEntity entity = new SimpleEntity(internalComponentManager, id);
                addEntityDataToEntity(entityData, entity);
                entities.add(entity);
                loadedEntities.add(entity);
            }
        }
        return loadedEntities;
    }

    private void addEntityDataToEntity(EntityData entityData, SimpleEntity entity) {
        for (ComponentData componentData : entityData.getComponentsData()) {
            Class<? extends Component> componentClass = componentData.getComponentClass();
            final Component component = createComponentFromData(componentData, componentClass);
            entity.entityValues.put(componentClass, component);
            entity.removedComponents.remove(componentClass);
        }
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

    private void unloadTheEntities(Collection<SimpleEntity> entitiesToUnload) {
        for (SimpleEntity entity : entitiesToUnload) {
            entity.exists = false;
            entities.remove(entity);
        }
    }

    private void tellRulesToStoreUnloadingEntities(Multimap<EntityRelevanceRule, SimpleEntity> entitiesToUnload) {
        for (Map.Entry<EntityRelevanceRule, Collection<SimpleEntity>> ruleEntities : entitiesToUnload.asMap().entrySet()) {
            EntityRelevanceRule rule = ruleEntities.getKey();
            rule.storeEntities(ruleEntities.getValue());
        }
    }

    private Multimap<EntityRelevanceRule, SimpleEntity> determineEntitiesToUnloadByRules() {
        Multimap<EntityRelevanceRule, SimpleEntity> entitiesToUnload = HashMultimap.create();
        for (EntityRelevanceRule entityRelevanceRule : entityRelevanceRules) {
            for (EntityRef entityRef : entityRelevanceRule.getNotRelevantEntities()) {
                entitiesToUnload.put(entityRelevanceRule, ((SimpleEntityRef) entityRef).getEntity());
            }
        }
        return entitiesToUnload;
    }

    @Override
    public EntityRef createEntity() {
        SimpleEntity entity = new SimpleEntity(internalComponentManager, ++maxId);
        entities.add(entity);
        return createSimpleEntityRef(entity, false);
    }

    @Override
    public EntityRef createEntityFromPrefab(String prefabName) {
        if (prefabManager == null)
            throw new RuntimeException("Unable to read prefab - prefab manager not defined");

        EntityData prefabByName = prefabManager.getPrefabByName(prefabName);

        SimpleEntity entity = new SimpleEntity(internalComponentManager, ++maxId, prefabByName);
        entities.add(entity);

        dispatchEntityListener.entitiesModified(Collections.singleton(entity));

        return createSimpleEntityRef(entity, false);
    }

    @Override
    public EntityRef createEntityFromData(EntityData entityData) {
        SimpleEntity entity = new SimpleEntity(internalComponentManager, ++maxId);
        addEntityDataToEntity(entityData, entity);
        entities.add(entity);

        dispatchEntityListener.entitiesModified(Collections.singleton(entity));

        return createSimpleEntityRef(entity, false);
    }

    @Override
    public EntityRef wrapEntityData(EntityData entityData) {
        SimpleEntity entity = new SimpleEntity(internalComponentManager, 0);
        entity.exists = false;
        addEntityDataToEntity(entityData, entity);
        return createSimpleEntityRef(entity, true);
    }

    @Override
    public EntityRef createNewEntityRef(EntityRef entityRef) {
        return createSimpleEntityRef(((SimpleEntityRef) entityRef).getEntity(), false);
    }

    @Override
    public boolean isSameEntity(EntityRef ref1, EntityRef ref2) {
        return ((SimpleEntityRef) ref1).getEntity() == ((SimpleEntityRef) ref2).getEntity();
    }

    @Override
    public EntityRef wrapEntityStub(SimpleEntity entity) {
        return createSimpleEntityRef(entity, false);
    }

    @Override
    public void destroyEntity(EntityRef entityRef) {
        Iterable<Class<? extends Component>> components = entityRef.listComponents();
        //noinspection unchecked
        entityRef.removeComponents(Iterables.toArray(components, Class.class));
        entityRef.saveChanges();
        SimpleEntity underlyingEntity = ((SimpleEntityRef) entityRef).getEntity();
        underlyingEntity.exists = false;
        entities.remove(underlyingEntity);
    }

    @Override
    public Iterable<EntityRef> getEntitiesWithComponents(final Class<? extends Component> component, final Class<? extends Component>... additionalComponents) {
        return Iterables.transform(Iterables.filter(entities,
                new Predicate<SimpleEntity>() {
                    @Override
                    public boolean apply(@Nullable SimpleEntity entity) {
                        if (!entity.hasComponent(component))
                            return false;

                        for (Class<? extends Component> additionalComponent : additionalComponents) {
                            if (!entity.hasComponent(additionalComponent))
                                return false;
                        }

                        return true;
                    }
                }),
                new Function<SimpleEntity, EntityRef>() {
                    @Override
                    public EntityRef apply(@Nullable SimpleEntity entity) {
                        return createSimpleEntityRef(entity, false);
                    }
                });
    }

    @Override
    public Iterable<EntityRef> getAllEntities() {
        return Iterables.transform(new HashSet<SimpleEntity>(entities),
                new Function<SimpleEntity, EntityRef>() {
                    @Override
                    public EntityRef apply(@Nullable SimpleEntity entity) {
                        return createSimpleEntityRef(entity, false);
                    }
                });
    }

    @Override
    public EntityData exposeEntityData(EntityRef entityRef) {
        return ((SimpleEntityRef) entityRef).getEntity();
    }

    private SimpleEntityRef createSimpleEntityRef(SimpleEntity entity, boolean readOnly) {
        return new SimpleEntityRef(internalComponentManager, dispatchEntityListener, dispatchEntityEventListener,
                entity, readOnly);
    }

    private class DispatchEntityListener implements EntityListener {
        private PriorityCollection<EntityListener> entityListeners = new PriorityCollection<EntityListener>();

        @Override
        public void entitiesModified(Iterable<SimpleEntity> entity) {
            for (EntityListener listener : entityListeners) {
                listener.entitiesModified(entity);
            }
        }
    }

    private class DispatchEntityEventListener implements EntityEventListener {
        private PriorityCollection<EntityEventListener> entityEventListeners = new PriorityCollection<EntityEventListener>();

        @Override
        public void eventSent(EntityRef entity, Event event) {
            for (EntityEventListener listener : entityEventListeners) {
                SimpleEntityRef newEntityRef = createSimpleEntityRef(((SimpleEntityRef) entity).getEntity(), false);
                listener.eventSent(newEntityRef, event);
            }
        }
    }
}
