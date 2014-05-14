package com.gempukku.secsy;

import com.gempukku.secsy.entity.EntityListener;
import com.gempukku.secsy.entity.EntityStorage;
import com.gempukku.secsy.entity.event.BeforeComponentDeactivated;
import com.gempukku.secsy.entity.event.ComponentActivated;
import com.gempukku.secsy.entity.io.EntityInputStream;
import com.gempukku.secsy.entity.io.EntityOutputStream;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class PersistingEntityManagerTest {
    private EventBus<Event> eventBus;
    private EntityFactory entityFactory;
    private EntityStorage entityStorage;
    private PersistingEntityManager manager;

    private EntityRef entity;

    @Before
    public void setup() {
        eventBus = Mockito.mock(EventBus.class);
        entityFactory = Mockito.mock(EntityFactory.class);
        entityStorage = Mockito.mock(EntityStorage.class);
        manager = new PersistingEntityManager(eventBus, entityFactory, entityStorage);

        entity = Mockito.mock(EntityRef.class);
        Mockito.when(entityFactory.createEntity(Mockito.<EntityListener>any())).thenReturn(entity);
    }

    @Test
    public void unloadEntity() {
        Mockito.when(entityStorage.getEntityId(entity)).thenReturn(1);
        final EntityOutputStream os = Mockito.mock(EntityOutputStream.class);
        manager.unloadEntity(os, entity);
        Mockito.verify(os).writeEntity(1, entity);
        Mockito.verify(entityStorage).removeEntity(entity);
        Mockito.verify(entityStorage).getEntityId(entity);
        Mockito.verifyNoMoreInteractions(os, eventBus, entityFactory, entityStorage);
    }

    @Test
    public void unloadEntityWithComponent() {
        Mockito.when(entity.listComponents()).thenReturn(Collections.singleton(SampleComponent.class));

        Mockito.when(entityStorage.getEntityId(entity)).thenReturn(1);
        final EntityOutputStream os = Mockito.mock(EntityOutputStream.class);
        manager.unloadEntity(os, entity);
        Mockito.verify(os).writeEntity(1, entity);
        Mockito.verify(entityStorage).removeEntity(entity);
        Mockito.verify(entityStorage).getEntityId(entity);
        Mockito.verify(eventBus).sendEvent(Mockito.same(entity), Mockito.argThat(
                new BaseMatcher<Event>() {
                    @Override
                    public boolean matches(Object o) {
                        BeforeComponentDeactivated event = (BeforeComponentDeactivated) o;
                        return event.getComponents().size() == 1
                                && event.getComponents().contains(SampleComponent.class);
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                }));
        Mockito.verifyNoMoreInteractions(os, eventBus, entityFactory, entityStorage);
    }

    @Test
    public void loadEntity() {
        EntityInputStream is = Mockito.mock(EntityInputStream.class);
        Mockito.when(is.readEntity()).thenReturn(new EntityInputStream.EntityWithId(1, entity));
        manager.loadEntity(is);
        Mockito.verify(is).readEntity();
        Mockito.verify(entityStorage).storeEntityWithId(1, entity);
        Mockito.verifyNoMoreInteractions(is, eventBus, entityFactory, entityStorage);
    }

    @Test
    public void loadEntityWithComponent() {
        Mockito.when(entity.listComponents()).thenReturn(Collections.singleton(SampleComponent.class));

        EntityInputStream is = Mockito.mock(EntityInputStream.class);
        Mockito.when(is.readEntity()).thenReturn(new EntityInputStream.EntityWithId(1, entity));
        manager.loadEntity(is);
        Mockito.verify(is).readEntity();
        Mockito.verify(entityStorage).storeEntityWithId(1, entity);
        Mockito.verify(eventBus).sendEvent(Mockito.same(entity), Mockito.argThat(
                new BaseMatcher<Event>() {
                    @Override
                    public boolean matches(Object o) {
                        ComponentActivated event = (ComponentActivated) o;
                        return event.getComponents().size() == 1
                                && event.getComponents().contains(SampleComponent.class);
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                }));
        Mockito.verifyNoMoreInteractions(is, eventBus, entityFactory, entityStorage);
    }
}
