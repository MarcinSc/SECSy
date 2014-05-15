package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.SampleComponent;
import com.gempukku.secsy.component.ComponentFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IsolationEntityFactoryTest {
    private ComponentFactory componentFactory;
    private Object valueObject;
    private IsolationEntityFactory factory;

    private EntityListener entityListener;

    @Before
    public void setup() throws NoSuchMethodException {
        componentFactory = Mockito.mock(ComponentFactory.class);
        valueObject = new Object();
        entityListener = Mockito.mock(EntityListener.class);
        SampleComponent createdComponent = Mockito.mock(SampleComponent.class);
        SampleComponent gotComponent = Mockito.mock(SampleComponent.class);
        Mockito.when(createdComponent.getComponentClass()).thenAnswer(
                new Answer<Class<? extends Component>>() {
                    @Override
                    public Class<? extends Component> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return SampleComponent.class;
                    }
                });
        Mockito.when(gotComponent.getComponentClass()).thenAnswer(
                new Answer<Class<? extends Component>>() {
                    @Override
                    public Class<? extends Component> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return SampleComponent.class;
                    }
                });
        Mockito.when(componentFactory.createComponentValueObject(SampleComponent.class)).thenReturn(valueObject);
        Mockito.when(componentFactory.createComponent(SampleComponent.class, valueObject)).thenReturn(createdComponent);
        Mockito.when(componentFactory.getComponent(SampleComponent.class, valueObject)).thenReturn(gotComponent);
        factory = new IsolationEntityFactory(componentFactory);

        Mockito.when(componentFactory.isNewComponent(createdComponent)).thenReturn(true);
        Mockito.when(componentFactory.isNewComponent(gotComponent)).thenReturn(false);
    }

    @Test
    public void entityLifeCycle() {
        final EntityRef entity = factory.createEntity(entityListener);
        assertNotNull(entity);
        assertTrue(entity.exists());
        factory.destroyEntity(entity);
        assertFalse(entity.exists());

        Mockito.verifyZeroInteractions(entityListener, componentFactory);
    }

    @Test
    public void getComponentBeforeAdd() {
        final EntityRef entity = factory.createEntity(entityListener);
        assertNull(entity.getComponent(SampleComponent.class));
    }

    @Test
    public void getComponentAfterAdd() {
        final EntityRef entity = factory.createEntity(entityListener);
        final Component component = entity.addComponent(SampleComponent.class);
        assertNull(entity.getComponent(SampleComponent.class));
        entity.saveComponents(component);
        assertNotNull(entity.getComponent(SampleComponent.class));
    }

    @Test
    public void removeComponentAfterAdd() {
        final EntityRef entity = factory.createEntity(entityListener);
        final Component component = entity.addComponent(SampleComponent.class);
        entity.saveComponents(component);
        entity.removeComponents(SampleComponent.class);
        assertNull(entity.getComponent(SampleComponent.class));
    }

    @Test
    public void listComponents() {
        final EntityRef entity = factory.createEntity(entityListener);
        assertTrue(entity.listComponents().isEmpty());
        final Component component = entity.addComponent(SampleComponent.class);
        entity.saveComponents(component);
        assertEquals(1, entity.listComponents().size());
        assertTrue(entity.listComponents().contains(SampleComponent.class));
    }

    @Test
    public void sendEvent() {
        final EntityRef entity = factory.createEntity(entityListener);
        Event event = Mockito.mock(Event.class);
        entity.send(event);
        Mockito.verify(entityListener).eventSent(entity, event);
        Mockito.verifyNoMoreInteractions(componentFactory, entityListener);
    }

    @Test
    public void setEntityListener() {
        final EntityRef entity = factory.createEntity(entityListener);
        EntityListener newListener = Mockito.mock(EntityListener.class);
        factory.setEntityListener(entity, newListener);
        Event event = Mockito.mock(Event.class);
        entity.send(event);
        Mockito.verify(newListener).eventSent(entity, event);
        Mockito.verifyNoMoreInteractions(componentFactory, entityListener);
    }

    @Test
    public void addComponentTwice() {
        final EntityRef entity = factory.createEntity(entityListener);
        final Component component1 = entity.addComponent(SampleComponent.class);
        final Component component2 = entity.addComponent(SampleComponent.class);
        entity.saveComponents(component1);
        try {
            entity.saveComponents(component2);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exp) {
            // Expected
        }
    }

    @Test
    public void saveComponentAfterRemoved() {
        final EntityRef entity = factory.createEntity(entityListener);
        final Component component = entity.addComponent(SampleComponent.class);
        entity.saveComponents(component);
        final Component copy = entity.getComponent(SampleComponent.class);
        entity.removeComponents(SampleComponent.class);
        try {
            entity.saveComponents(copy);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exp) {
            // Expected
        }
    }

    @Test
    public void removeComponentBeforeAdd() {
        final EntityRef entity = factory.createEntity(entityListener);
        try {
            entity.removeComponents(SampleComponent.class);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exp) {
            // Expected
        }
    }

    @Test
    public void addComponentCheckListener() {
        final EntityRef entity = factory.createEntity(entityListener);
        final Component component = entity.addComponent(SampleComponent.class);
        Mockito.verify(componentFactory).createComponentValueObject(SampleComponent.class);
        Mockito.verify(componentFactory).createComponent(SampleComponent.class, valueObject);
        Mockito.verifyNoMoreInteractions(entityListener, componentFactory);
        entity.saveComponents(component);
        Mockito.verify(componentFactory, Mockito.atLeast(0)).isNewComponent(Mockito.any(SampleComponent.class));
        Mockito.verify(entityListener).afterComponentAdded(Mockito.same(entity), Mockito.argThat(new SampleComponentMatcher()));
        Mockito.verify(componentFactory).saveComponent(component, valueObject);
        Mockito.verifyNoMoreInteractions(entityListener, componentFactory);
    }

    @Test
    public void updateComponentCheckListener() {
        final EntityRef entity = factory.createEntity(entityListener);
        final Component component = entity.addComponent(SampleComponent.class);
        entity.saveComponents(component);

        Mockito.verify(componentFactory, Mockito.atLeast(0)).isNewComponent(component);
        Mockito.verify(componentFactory).createComponentValueObject(SampleComponent.class);
        Mockito.verify(componentFactory).createComponent(SampleComponent.class, valueObject);
        Mockito.verify(entityListener).afterComponentAdded(Mockito.same(entity), Mockito.argThat(new SampleComponentMatcher()));
        Mockito.verify(componentFactory).saveComponent(component, valueObject);
        Mockito.verifyNoMoreInteractions(entityListener, componentFactory);

        final Component copy = entity.getComponent(SampleComponent.class);
        entity.saveComponents(copy);

        Mockito.verify(componentFactory, Mockito.atLeast(0)).isNewComponent(copy);
        Mockito.verify(entityListener).afterComponentUpdated(Mockito.same(entity), Mockito.argThat(new SampleComponentMatcher()));
        Mockito.verify(componentFactory).getComponent(SampleComponent.class, valueObject);
        Mockito.verify(componentFactory).saveComponent(copy, valueObject);
        Mockito.verifyNoMoreInteractions(entityListener, componentFactory);
    }

    private class SampleComponentMatcher extends BaseMatcher<Set<Class<? extends Component>>> {
        @Override
        public boolean matches(Object o) {
            Set<Class<? extends Component>> components = (Set<Class<? extends Component>>) o;
            return components.size() == 1
                    && components.contains(SampleComponent.class);
        }

        @Override
        public void describeTo(Description description) {
        }
    }
}
