package com.gempukku.secsy;

import com.gempukku.secsy.entity.EntityListener;
import com.gempukku.secsy.entity.EntityStorage;
import com.gempukku.secsy.event.BeforeComponentRemoved;
import com.gempukku.secsy.event.Event;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class SimpleEntityManagerTest {
    private EventBus<Event> eventBus;
    private EntityFactory entityFactory;
    private EntityStorage entityStorage;
    private SimpleEntityManager manager;

    private EntityRef entity;

    @Before
    public void setup() {
        eventBus = Mockito.mock(EventBus.class);
        entityFactory = Mockito.mock(EntityFactory.class);
        entityStorage = Mockito.mock(EntityStorage.class);
        manager = new SimpleEntityManager(eventBus, entityFactory, entityStorage);

        entity = Mockito.mock(EntityRef.class);
        Mockito.when(entityFactory.createEntity(Mockito.<EntityListener>any())).thenReturn(entity);
    }

    @Test
    public void create() {
        final EntityRef<Event> createdEntity = manager.create();
        assertSame(entity, createdEntity);
        Mockito.verify(entityFactory).createEntity(Mockito.<EntityListener>any());
        Mockito.verify(entityStorage).storeNewEntity(createdEntity);
        Mockito.verifyNoMoreInteractions(eventBus, entityFactory, entityStorage);
    }

    @Test
    public void destroy() {
        manager.destroy(entity);
        Mockito.verify(entityFactory).destroyEntity(entity);
        Mockito.verify(entityStorage).removeEntity(entity);
        Mockito.verifyNoMoreInteractions(eventBus, entityFactory, entityStorage);
    }

    @Test
    public void destroyWithComponent() {
        Mockito.when(entity.listComponents()).thenReturn(Collections.singleton(SampleComponent.class));

        manager.destroy(entity);
        Mockito.verify(eventBus).sendEvent(Mockito.same(entity), Mockito.argThat(
                new BaseMatcher<Event>() {
                    @Override
                    public boolean matches(Object o) {
                        BeforeComponentRemoved event = (BeforeComponentRemoved) o;
                        return event.getComponents().size() == 1
                                && event.getComponents().contains(SampleComponent.class);
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                }));
        Mockito.verify(entityFactory).destroyEntity(entity);
        Mockito.verify(entityStorage).removeEntity(entity);
        Mockito.verifyNoMoreInteractions(eventBus, entityFactory, entityStorage);
    }

    @Test
    public void getEntityId() {
        Mockito.when(entityStorage.getEntityId(entity)).thenReturn(1);

        assertEquals(1, manager.getEntityId(entity));

        Mockito.verify(entityStorage).getEntityId(entity);
        Mockito.verifyNoMoreInteractions(eventBus, entityFactory, entityStorage);
    }
}
