package com.gempukku.secsy.entity;

import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.SampleComponent;
import com.gempukku.secsy.event.Event;
import org.junit.Test;
import org.mockito.Mockito;

public class IgnoreStateListenerTest {
    @Test
    public void testingAllEmptyMethods() {
        final EntityRef<Event> entity = Mockito.mock(EntityRef.class);

        IgnoreStateListener listener = new IgnoreStateListener();
        listener.afterComponentAdded(entity, SampleComponent.class);
        listener.afterComponentUpdated(entity, SampleComponent.class);
        listener.beforeComponentRemoved(entity, SampleComponent.class);
        listener.eventSent(entity, Mockito.mock(Event.class));

        Mockito.verifyZeroInteractions(entity);
    }
}
