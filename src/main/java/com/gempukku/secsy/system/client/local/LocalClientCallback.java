package com.gempukku.secsy.system.client.local;

import com.gempukku.secsy.event.EventSerializer;
import com.gempukku.secsy.system.client.host.ClientCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class LocalClientCallback<E> implements ClientCallback<E> {
    private ClientCallback<E> delegate;
    private EventSerializer<E> eventSerializer;

    public LocalClientCallback(ClientCallback<E> delegate, EventSerializer<E> eventSerializer) {
        this.delegate = delegate;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public void sendEvent(int entityId, E event) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        eventSerializer.serializeEvent(event, os);

        final E eventCopy = eventSerializer.deserializeEvent(new ByteArrayInputStream(os.toByteArray()));
        delegate.sendEvent(entityId, eventCopy);
    }
}
