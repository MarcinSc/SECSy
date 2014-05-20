package com.gempukku.secsy.entity.io;

import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.system.client.host.EntityComponentFieldFilter;

import java.io.InputStream;
import java.io.OutputStream;

public interface EntitySerializer<E> {
    public void serializeEntity(EntityRef<E> entity, OutputStream outputStream, EntityComponentFieldFilter<E> filter);
    public EntityState<E> deserializeEntity(InputStream inputStream);
}
