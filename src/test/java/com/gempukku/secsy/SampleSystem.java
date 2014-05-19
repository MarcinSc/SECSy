package com.gempukku.secsy;

import com.gempukku.secsy.annotation.ReceiveEvent;

import static org.junit.Assert.assertNotNull;

public class SampleSystem {
    public int validCalls;
    public int invalidCalls;

    @ReceiveEvent
    public void validMethod(SampleEvent event, EntityRef<Event> entity, SampleComponent sampleComponent) {
        assertNotNull(event);
        assertNotNull(entity);
        assertNotNull(sampleComponent);

        validCalls++;
    }

    @ReceiveEvent
    private void invalidMethodPrivate(SampleEvent event, EntityRef<Event> entity, SampleComponent sampleComponent) {
        invalidCalls++;
    }

    @ReceiveEvent
    public int invalidMethodReturnsInt(SampleEvent event, EntityRef<Event> entity, SampleComponent sampleComponent) {
        invalidCalls++;
        return 0;
    }

    @ReceiveEvent
    public void invalidMethodMissingEntity(SampleEvent event, SampleComponent sampleComponent) {
        invalidCalls++;
    }

    @ReceiveEvent
    public void invalidMethodMissingEvent(EntityRef<Event> entity, SampleComponent sampleComponent) {
        invalidCalls++;
    }

    @ReceiveEvent
    public void invalidMethodAdditionalParameter(SampleEvent event, EntityRef<Event> entity, Object object, SampleComponent sampleComponent) {
        invalidCalls++;
    }

    @ReceiveEvent
    public void throwingExceptionMethod(SampleEvent event, EntityRef<Event> entity, SampleComponent2 sampleComponent2) {
        validCalls++;
        throw new RuntimeException();
    }

}
