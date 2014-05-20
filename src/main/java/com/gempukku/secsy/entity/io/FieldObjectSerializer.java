package com.gempukku.secsy.entity.io;

import java.io.InputStream;
import java.io.OutputStream;

public interface FieldObjectSerializer {
    public <T> void serializeField(OutputStream stream, String fieldName, Class<T> clazz, T value);
    public void deserializeStream(InputStream stream, FieldObjectVisitor visitor);

    public interface FieldObjectVisitor {
        public <T> void deserializedField(String fieldName, Class<T> clazz, T value);
    }
}
