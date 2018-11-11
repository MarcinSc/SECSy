package com.gempukku.secsy.entity;

import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.entity.component.ComponentFieldConverter;

@RegisterSystem(
        shared = ComponentFieldConverter.class)
public class SimpleComponentFieldConverter implements ComponentFieldConverter {
    @Override
    public boolean hasConverterForType(Class<?> clazz) {
        return false;
    }

    @Override
    public <T> T convertTo(Object value, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T convertTo(Object value, Class<T> clazz, Class<?> containedClass) {
        return null;
    }

    @Override
    public <T> String convertFrom(T value, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T getDefaultValue(Class<T> clazz) {
        return null;
    }
}
