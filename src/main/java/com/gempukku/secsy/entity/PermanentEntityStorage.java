package com.gempukku.secsy.entity;

import com.gempukku.secsy.entity.io.EntityInputStream;
import com.gempukku.secsy.entity.io.EntityOutputStream;

public interface PermanentEntityStorage<E> extends EntityStorage<E> {
    public void deactivateEntity(EntityOutputStream<E> entityOutputStream);

    public void activateEntity(EntityInputStream<E> entityInputStream);
}
