package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.EventBus;
import com.gempukku.secsy.SampleComponent;
import com.gempukku.secsy.entity.event.BeforeComponentRemoved;
import com.gempukku.secsy.entity.event.ComponentAdded;
import com.gempukku.secsy.entity.event.ComponentUpdated;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class NormalStateListenerTest {
    private NormalStateListener listener;
    private EventBus eventBus;
    private EntityRef entity;

    @Before
    public void setup() {
        eventBus = Mockito.mock(EventBus.class);
        entity = Mockito.mock(EntityRef.class);
        listener = new NormalStateListener(eventBus);
    }

    @Test
    public void componentAdded() {
        listener.afterComponentAdded(entity, Collections.<Class<? extends Component>>singleton(SampleComponent.class));
        Mockito.verify(eventBus).sendEvent(Mockito.same(entity), Mockito.argThat(
                new BaseMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        ComponentAdded event = (ComponentAdded) o;
                        return event.getComponents().size() == 1
                                && event.getComponents().contains(SampleComponent.class);
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                }));
        Mockito.verifyNoMoreInteractions(eventBus, entity);
    }

    @Test
    public void componentUpdated() {
        listener.afterComponentUpdated(entity, Collections.<Class<? extends Component>>singleton(SampleComponent.class));
        Mockito.verify(eventBus).sendEvent(Mockito.same(entity), Mockito.argThat(
                new BaseMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        ComponentUpdated event = (ComponentUpdated) o;
                        return event.getComponents().size() == 1
                                && event.getComponents().contains(SampleComponent.class);
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                }));
        Mockito.verifyNoMoreInteractions(eventBus, entity);
    }

    @Test
    public void componentRemoved() {
        listener.beforeComponentRemoved(entity, Collections.<Class<? extends Component>>singleton(SampleComponent.class));
        Mockito.verify(eventBus).sendEvent(Mockito.same(entity), Mockito.argThat(
                new BaseMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        BeforeComponentRemoved event = (BeforeComponentRemoved) o;
                        return (event.getComponents().size() == 1)
                                && event.getComponents().contains(SampleComponent.class);
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                }));
        Mockito.verifyNoMoreInteractions(eventBus, entity);
    }

    @Test
    public void eventSent() {
        final Event event = Mockito.mock(Event.class);
        listener.eventSent(entity, event);
        Mockito.verify(eventBus).sendEvent(entity, event);
    }
}
