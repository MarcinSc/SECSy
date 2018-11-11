package com.gempukku.secsy.entity.dispatch;

import com.gempukku.secsy.context.SystemContext;
import com.gempukku.secsy.context.system.ShareSystemInitializer;
import com.gempukku.secsy.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AnnotationDrivenEventDispatcherTest {
    private AnnotationDrivenEventDispatcher dispatcher;
    private SampleSystem sampleSystem;
    private EntityRef entity;

    @Before
    public void setup() {
        sampleSystem = new SampleSystem();
        dispatcher = new AnnotationDrivenEventDispatcher();
        SystemContext context = Mockito.mock(SystemContext.class);
        Mockito.when(context.getSystems()).thenReturn(Arrays.<Object>asList(sampleSystem));
        ShareSystemInitializer initializer = new ShareSystemInitializer();
        Map<Class<?>, Object> systems = new HashMap<Class<?>, Object>();
        systems.put(SystemContext.class, context);
        systems.put(InternalEntityManager.class, Mockito.mock(InternalEntityManager.class));
        initializer.initializeObjects(Collections.<Object>singleton(dispatcher), systems);
        dispatcher.initialize();

        entity = Mockito.mock(EntityRef.class);
    }

    @Test
    public void matchingEntityCall() {
        Mockito.when(entity.hasComponent(SampleComponent.class)).thenReturn(true);
        Mockito.when(entity.getComponent(SampleComponent.class)).thenReturn(Mockito.mock(SampleComponent.class));

        dispatcher.eventSent(entity, new SampleEvent());

        assertEquals(0, sampleSystem.invalidCalls);
        assertEquals(1, sampleSystem.validCalls);
    }

    @Test
    public void notMatchingEntityCall() {
        Mockito.when(entity.hasComponent(SampleComponent.class)).thenReturn(false);

        dispatcher.eventSent(entity, new SampleEvent());

        assertEquals(0, sampleSystem.invalidCalls);
        assertEquals(0, sampleSystem.validCalls);
    }

    @Test
    public void throwingExceptionCall() {
        Mockito.when(entity.hasComponent(SampleComponent2.class)).thenReturn(true);
        Mockito.when(entity.getComponent(SampleComponent2.class)).thenReturn(Mockito.mock(SampleComponent2.class));

        try {
            dispatcher.eventSent(entity, new SampleEvent());
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