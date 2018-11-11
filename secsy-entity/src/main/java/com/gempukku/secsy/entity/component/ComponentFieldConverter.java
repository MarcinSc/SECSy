package com.gempukku.secsy.entity.component;

public interface ComponentFieldConverter {
    boolean hasConverterForType(Class<?> clazz);

    <T> T convertTo(Object value, Class<T> clazz);

    <T> T convertTo(Object value, Class<T> clazz, Class<?> containedClass);

    <T> Object convertFrom(T value, Class<T> clazz);

    <T> T getDefaultValue(Class<T> clazz);
}
