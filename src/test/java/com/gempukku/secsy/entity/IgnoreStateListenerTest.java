package com.gempukku.secsy.entity;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.SampleComponent;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class IgnoreStateListenerTest {
    @Test
    public void testingAllEmptyMethods() {
        final EntityRef<Event> entity = Mockito.mock(EntityRef.class);

        IgnoreStateListener listener = new IgnoreStateListener();
        listener.afterComponentAdded(entity, Collections.<Class<? extends Component>>singleton(SampleComponent.class));
        listener.afterComponentUpdated(entity, Collections.<Class<? extends Component>>singleton(SampleComponent.class));
        listener.beforeComponentRemoved(entity, Collections.<Class<? extends Component>>singleton(SampleComponent.class));
        listener.eventSent(entity, Mockito.mock(Event.class));

        Mockito.verifyZeroInteractions(entity);
    }
}
