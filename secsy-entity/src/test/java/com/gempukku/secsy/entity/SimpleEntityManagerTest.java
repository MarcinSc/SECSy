package com.gempukku.secsy.entity;

import com.gempukku.secsy.context.system.ShareSystemInitializer;
import com.gempukku.secsy.entity.component.map.MapAnnotationDrivenProxyComponentManager;
import com.gempukku.secsy.entity.event.AfterComponentAdded;
import com.gempukku.secsy.entity.event.AfterComponentUpdated;
import com.gempukku.secsy.entity.event.BeforeComponentRemoved;
import com.gempukku.secsy.entity.event.Event;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SimpleEntityManagerTest {
    private SimpleEntityManager simpleEntityManager;

    @Before
    public void setup() {
        MapAnnotationDrivenProxyComponentManager componentManager = new MapAnnotationDrivenProxyComponentManager();
        simpleEntityManager = new SimpleEntityManager();
        ShareSystemInitializer<Object> shareSystemInitializer = new ShareSystemInitializer<>();
        shareSystemInitializer.initializeSystems(Arrays.asList(componentManager, simpleEntityManager));
    }

    @Test
    public void createEntity() {
        EntityRef entity = simpleEntityManager.createEntity();
        assertFalse(entity.hasComponent(SampleComponent.class));
    }

    @Test
    public void addComponentInteractingMultipleEntityRefs() {
        EntityRef source = simpleEntityManager.createEntity();

        EntityRef copy = simpleEntityManager.createNewEntityRef(source);
        SampleComponent component = copy.createComponent(SampleComponent.class);

        assertFalse(copy.hasComponent(SampleComponent.class));
        assertFalse(source.hasComponent(SampleComponent.class));

        copy.saveComponents(component);
        assertTrue(copy.hasComponent(SampleComponent.class));
        assertTrue(source.hasComponent(SampleComponent.class));
    }

    @Test
    public void editComponentInteractingMultipleEntityRefs() {
        EntityRef source = simpleEntityManager.createEntity();

        EntityRef copy = simpleEntityManager.createNewEntityRef(source);
        SampleComponent component = copy.createComponent(SampleComponent.class);
        copy.saveComponents(component);

        SampleComponent sourceComponent = source.getComponent(SampleComponent.class);
        assertNotNull(sourceComponent);
        assertNull(sourceComponent.getValue());

        // Unsaved change is not visible in the source
        component.setValue("value");
        assertNull(sourceComponent.getValue());
        assertEquals("value", component.getValue());

        // Changes are immediately visible in the source after save
        copy.saveComponents(component);
        assertEquals("value", sourceComponent.getValue());
        assertEquals("value", component.getValue());
    }

    @Test
    public void removeComponentInteractingMultipleEntityRefs() {
        EntityRef source = simpleEntityManager.createEntity();

        EntityRef copy = simpleEntityManager.createNewEntityRef(source);
        SampleComponent component = copy.createComponent(SampleComponent.class);
        copy.saveComponents(component);

        assertTrue(source.hasComponent(SampleComponent.class));

        //noinspection unchecked
        copy.removeComponents(SampleComponent.class);

        assertFalse(source.hasComponent(SampleComponent.class));
    }

    @Test
    public void destroyEntityMakesOtherEntityRefsNotExist() {
        EntityRef source = simpleEntityManager.createEntity();

        EntityRef copy = simpleEntityManager.createNewEntityRef(source);

        assertTrue(source.exists());
        assertTrue(copy.exists());

        simpleEntityManager.destroyEntity(copy);
        assertFalse(source.exists());
        assertFalse(copy.exists());
    }

    @Test
    public void notifyOnAddingComponent() {
        Listener listener = new Listener();
        simpleEntityManager.addEntityEventListener(listener);

        EntityRef entity = simpleEntityManager.createEntity();
        SampleComponent component = entity.createComponent(SampleComponent.class);

        assertEquals(0, listener.events.size());

        entity.saveComponents(component);

        assertEquals(1, listener.events.size());
        EntityAndEvent entityAndEvent = listener.events.get(0);

        simpleEntityManager.isSameEntity(entityAndEvent.entity, entity);
        assertTrue(entityAndEvent.event instanceof AfterComponentAdded);
    }

    @Test
    public void notifyOnUpdatingComponent() {
        EntityRef entity = simpleEntityManager.createEntity();
        SampleComponent component = entity.createComponent(SampleComponent.class);
        entity.saveComponents(component);

        Listener listener = new Listener();
        simpleEntityManager.addEntityEventListener(listener);

        component.setValue("value");

        assertEquals(0, listener.events.size());

        entity.saveComponents(component);

        assertEquals(1, listener.events.size());
        EntityAndEvent entityAndEvent = listener.events.get(0);

        simpleEntityManager.isSameEntity(entityAndEvent.entity, entity);
        assertTrue(entityAndEvent.event instanceof AfterComponentUpdated);

        AfterComponentUpdated event = (AfterComponentUpdated) entityAndEvent.event;
        assertTrue(event.getModifiedComponents().contains(SampleComponent.class));
        assertNull(event.getOldComponent(SampleComponent.class).getValue());
        assertEquals("value", event.getNewComponent(SampleComponent.class).getValue());
    }

    @Test
    public void notifyOnRemovingComponent() {
        EntityRef entity = simpleEntityManager.createEntity();
        SampleComponent component = entity.createComponent(SampleComponent.class);
        entity.saveComponents(component);

        Listener listener = new Listener();
        simpleEntityManager.addEntityEventListener(listener);

        assertEquals(0, listener.events.size());

        entity.removeComponents(SampleComponent.class);

        assertEquals(1, listener.events.size());
        EntityAndEvent entityAndEvent = listener.events.get(0);

        simpleEntityManager.isSameEntity(entityAndEvent.entity, entity);
        assertTrue(entityAndEvent.event instanceof BeforeComponentRemoved);
    }

    private class Listener implements EntityEventListener {
        private List<EntityAndEvent> events = new LinkedList<>();

        @Override
        public void eventSent(EntityRef entity, Event event) {
            events.add(new EntityAndEvent(entity, event));
        }
    }

    private static class EntityAndEvent {
        public final EntityRef entity;
        public final Event event;

        public EntityAndEvent(EntityRef entity, Event event) {
            this.entity = entity;
            this.event = event;
        }
    }
}