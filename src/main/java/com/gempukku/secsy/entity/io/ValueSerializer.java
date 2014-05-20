package com.gempukku.secsy.entity.io;

import java.io.InputStream;
import java.io.OutputStream;

public interface ValueSerializer {
    public <T> void serializeValue(OutputStream outputStream, Class<T> clazz, T value);
    public <T> T deserializeValue(InputStream inputStream, Class<T> clazz);
}
