package com.gempukku.secsy.entity;

import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.context.system.ShareSystemInitializer;
import com.gempukku.secsy.entity.component.MapNamingConventionProxyComponentManager;
import com.gempukku.secsy.entity.event.Event;
import com.gempukku.secsy.entity.game.InternalGameLoop;
import com.gempukku.secsy.entity.game.InternalGameLoopListener;
import com.gempukku.secsy.entity.serialization.ComponentInformation;
import com.gempukku.secsy.entity.serialization.EntityInformation;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SimpleEntityManagerTest {
    private SimpleEntityManager simpleEntityManager;

    @Before
    public void setup() {
        MapNamingConventionProxyComponentManager componentManager = new MapNamingConventionProxyComponentManager();
        SimpleEntityComponentFieldHandler fieldHandler = new SimpleEntityComponentFieldHandler();
        SimpleComponentFieldConverter fieldConverter = new SimpleComponentFieldConverter();
        simpleEntityManager = new SimpleEntityManager();
        TestPrefabManager testPrefabManager = new TestPrefabManager();
        final EntityInformation entityInformation = new EntityInformation();
        ComponentInformation componentInformation = new ComponentInformation(SampleComponent.class);
        componentInformation.addField("value", "a");
        entityInformation.addComponent(componentInformation);
        testPrefabManager.addPrefab("prefab", entityInformation);
        ShareSystemInitializer shareSystemInitializer = new ShareSystemInitializer();
        Collection<Object> systems = Arrays.asList(componentManager, simpleEntityManager, testPrefabManager, fieldHandler, fieldConverter, new MockInternalGameLoop());
        Map<Class<?>, Object> systemMap = shareSystemInitializer.extractSystems(systems);
        shareSystemInitializer.initializeObjects(systems, systemMap);
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

        copy.saveChanges();
        assertTrue(copy.hasComponent(SampleComponent.class));
        assertTrue(source.hasComponent(SampleComponent.class));
    }

    @Test
    public void addComponentWithSaveInteractingMultipleEntityRefs() {
        EntityRef source = simpleEntityManager.createEntity();

        EntityRef copy = simpleEntityManager.createNewEntityRef(source);
        SampleComponent component = copy.createComponent(SampleComponent.class);
        component.setValue("value");

        assertFalse(copy.hasComponent(SampleComponent.class));
        assertFalse(source.hasComponent(SampleComponent.class));

        copy.saveChanges();
        assertTrue(copy.hasComponent(SampleComponent.class));
        assertEquals("value", copy.getComponent(SampleComponent.class).getValue());
        assertTrue(source.hasComponent(SampleComponent.class));
        assertEquals("value", source.getComponent(SampleComponent.class).getValue());
    }

    @Test
    public void editComponentInteractingMultipleEntityRefs() {
        EntityRef source = simpleEntityManager.createEntity();

        EntityRef copy = simpleEntityManager.createNewEntityRef(source);
        copy.createComponent(SampleComponent.class);
        copy.saveChanges();

        SampleComponent sourceComponent = source.getComponent(SampleComponent.class);
        assertNotNull(sourceComponent);
        assertNull(sourceComponent.getValue());

        // Unsaved change is not visible in the source
        SampleComponent component = copy.getComponent(SampleComponent.class);
        component.setValue("value");
        assertNull(sourceComponent.getValue());
        assertEquals("value", component.getValue());

        // Changes are immediately visible in the source after save
        copy.saveChanges();

        component = copy.getComponent(SampleComponent.class);
        assertEquals("value", sourceComponent.getValue());
        assertEquals("value", component.getValue());
    }

    @Test
    public void removeComponentInteractingMultipleEntityRefs() {
        EntityRef source = simpleEntityManager.createEntity();

        EntityRef copy = simpleEntityManager.createNewEntityRef(source);
        SampleComponent component = copy.createComponent(SampleComponent.class);
        copy.saveChanges();

        assertTrue(source.hasComponent(SampleComponent.class));

        //noinspection unchecked
        copy.removeComponents(SampleComponent.class);
        copy.saveChanges();

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
    public void createEntityDataWrapper() {
        Listener listener = new Listener();
        simpleEntityManager.addEntityEventListener(listener);

        EntityInformation data = new EntityInformation();
        ComponentInformation componentInformation = new ComponentInformation(SampleComponent.class);
        componentInformation.addField("value", "a");
        data.addComponent(componentInformation);

        EntityRef result = simpleEntityManager.wrapEntityData(data);

        assertEquals(0, listener.events.size());

        assertEquals("a", result.getComponent(SampleComponent.class).getValue());
    }

    @Test
    public void createFromPrefab() {
        EntityRef entity = simpleEntityManager.createEntityFromPrefab("prefab");
        assertNotNull(entity);
        SampleComponent component = entity.getComponent(SampleComponent.class);
        assertEquals("a", component.getValue());

        assertTrue(entity.hasComponent(SampleComponent.class));
        assertFalse(entity.hasComponent(SampleComponent2.class));

        try {
            entity.createComponent(SampleComponent.class);
            fail("Expected exception");
        } catch (IllegalStateException exp) {
            // expected
        }

        component.setValue("b");
        entity.saveChanges();

        assertEquals("b", entity.getComponent(SampleComponent.class).getValue());

        assertEquals("a", simpleEntityManager.createEntityFromPrefab("prefab").getComponent(SampleComponent.class).getValue());

        entity.removeComponents(SampleComponent.class);
        entity.saveChanges();

        assertFalse(entity.hasComponent(SampleComponent.class));
        assertNull(entity.getComponent(SampleComponent.class));

        SampleComponent newComponent = entity.createComponent(SampleComponent.class);
        newComponent.setValue("b");
        entity.saveChanges();

        assertEquals("b", entity.getComponent(SampleComponent.class).getValue());
    }

    private class Listener implements EntityEventListener {
        private List<EntityAndEvent> events = new LinkedList<EntityAndEvent>();

        @Override
        public void eventSent(EntityRef entity, Event event) {
            events.add(new EntityAndEvent(entity, event));
        }
    }

    @RegisterSystem(
            shared = InternalGameLoop.class)
    public static class MockInternalGameLoop implements InternalGameLoop {
        @Override
        public void addInternalGameLoopListener(InternalGameLoopListener internalGameLoopListener) {

        }

        @Override
        public void removeInternalGameLooplListener(InternalGameLoopListener internalGameLoopListener) {

        }

        @Override
        public void processUpdate() {

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