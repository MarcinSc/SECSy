package com.gempukku.secsy.event;

import java.io.InputStream;
import java.io.OutputStream;

public interface EventSerializer<E> {
    public void serializeEvent(E event, OutputStream outputStream);
    public E deserializeEvent(InputStream inputStream);
}
