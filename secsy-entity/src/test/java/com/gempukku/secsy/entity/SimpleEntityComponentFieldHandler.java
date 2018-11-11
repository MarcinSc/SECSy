package com.gempukku.secsy.entity;

import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.entity.component.EntityComponentFieldHandler;

@RegisterSystem(
        shared = EntityComponentFieldHandler.class)
public class SimpleEntityComponentFieldHandler implements EntityComponentFieldHandler {
    @Override
    public <T> T copyFromEntity(T value, Class<T> clazz) {
        return value;
    }

    @Override
    public <T> T storeIntoEntity(T oldValue, T newValue, Class<T> clazz) {
        return newValue;
    }
}
