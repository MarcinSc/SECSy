package com.gempukku.secsy;

import com.gempukku.secsy.entity.io.EntityInputStream;
import com.gempukku.secsy.entity.io.EntityOutputStream;

public interface EntityPersister<E> {
    public void unloadEntity(EntityOutputStream<E> outputStream, EntityRef<E> entity);

    public void loadEntity(EntityInputStream<E> inputStream);
}
