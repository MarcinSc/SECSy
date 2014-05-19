package com.gempukku.secsy.annotation;

import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.SampleComponent;
import com.gempukku.secsy.SampleComponent2;
import com.gempukku.secsy.SampleEvent;
import com.gempukku.secsy.SampleSystem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AnnotationDrivenReflectionEventDispatcherTest {
    private AnnotationDrivenReflectionEventDispatcher dispatcher;
    private SampleSystem sampleSystem;
    private EntityRef<Event> entity;

    @Before
    public void setup() {
        sampleSystem = new SampleSystem();
        dispatcher = new AnnotationDrivenReflectionEventDispatcher();
        dispatcher.scanSystem(sampleSystem);

        entity = Mockito.mock(EntityRef.class);
    }

    @Test
    public void matchingEntityCall() {
        Mockito.when(entity.hasComponent(SampleComponent.class)).thenReturn(true);
        Mockito.when(entity.getComponent(SampleComponent.class)).thenReturn(Mockito.mock(SampleComponent.class));

        dispatcher.eventReceived(entity, new SampleEvent());

        assertEquals(0, sampleSystem.invalidCalls);
        assertEquals(1, sampleSystem.validCalls);
    }

    @Test
    public void notMatchingEntityCall() {
        Mockito.when(entity.hasComponent(SampleComponent.class)).thenReturn(false);

        dispatcher.eventReceived(entity, new SampleEvent());

        assertEquals(0, sampleSystem.invalidCalls);
        assertEquals(0, sampleSystem.validCalls);
    }

    @Test
    public void throwingExceptionCall() {
        Mockito.when(entity.hasComponent(SampleComponent2.class)).thenReturn(true);
        Mockito.when(entity.getComponent(SampleComponent2.class)).thenReturn(Mockito.mock(SampleComponent2.class));

        try {
            dispatcher.eventReceived(entity, new SampleEvent());
            fail("Expected RuntimeException");
        } catch (RuntimeException exp) {
            final Throwable cause = exp.getCause();
            assertTrue(cause instanceof InvocationTargetException);
            final Throwable realCause = cause.getCause();
            assertTrue(realCause instanceof RuntimeException);
            assertEquals(0, sampleSystem.invalidCalls);
            assertEquals(1, sampleSystem.validCalls);
        }
    }
}
