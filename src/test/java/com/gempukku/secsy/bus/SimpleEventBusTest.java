package com.gempukku.secsy.bus;

import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.EventListener;
import org.junit.Test;
import org.mockito.Mockito;

public class SimpleEventBusTest {
    private SimpleEventBus<Object> bus = new SimpleEventBus<>();
    private EntityRef<Object> entity = Mockito.mock(EntityRef.class);

    @Test
    public void sendingEventNoListeners() {
        bus.sendEvent(entity, new Object());
    }

    @Test
    public void sendingEventToListener() {
        final EventListener<Object> listener = Mockito.mock(EventListener.class);
        bus.addEventListener(listener);
        Object event = new Object();
        bus.sendEvent(entity, event);

        Mockito.verify(listener).eventReceived(entity, event);
        Mockito.verifyNoMoreInteractions(listener);
    }
    
    @Test
    public void sendingEventAfterListenerRemoved() {
        final EventListener<Object> listener = Mockito.mock(EventListener.class);
        bus.addEventListener(listener);
        bus.removeEventListener(listener);
        Object event = new Object();
        bus.sendEvent(entity, event);

        Mockito.verifyNoMoreInteractions(listener);
    }
}
