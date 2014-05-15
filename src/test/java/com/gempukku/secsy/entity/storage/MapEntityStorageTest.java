package com.gempukku.secsy.entity.storage;

import com.gempukku.secsy.EntityRef;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class MapEntityStorageTest {
    private MapEntityStorage mapEntityStorage = new MapEntityStorage();

    @Test
    public void createNewEntityIdAssigned() {
        final EntityRef entity = Mockito.mock(EntityRef.class);
        final int id = mapEntityStorage.storeNewEntity(entity);
        assertEquals(id, mapEntityStorage.getEntityId(entity));
        Mockito.verifyNoMoreInteractions(entity);
    }

    @Test
    public void getEntityById() {
        final EntityRef entity = Mockito.mock(EntityRef.class);
        final int id = mapEntityStorage.storeNewEntity(entity);
        assertSame(entity, mapEntityStorage.getEntityById(id));
        Mockito.verifyNoMoreInteractions(entity);
    }

    @Test
    public void unknownEntity() {
        final EntityRef entity = mapEntityStorage.getEntityById(1);
        assertFalse(entity.exists());
    }

    @Test
    public void storeAssignedId() {
        final EntityRef entity = Mockito.mock(EntityRef.class);
        mapEntityStorage.storeEntityWithId(10, entity);
        assertEquals(10, mapEntityStorage.getEntityId(entity));
        assertSame(entity, mapEntityStorage.getEntityById(10));
        Mockito.verifyNoMoreInteractions(entity);
    }

    @Test
    public void removeEntity() {
        final EntityRef entity = Mockito.mock(EntityRef.class);
        final int id = mapEntityStorage.storeNewEntity(entity);
        mapEntityStorage.removeEntity(entity);
        assertFalse(mapEntityStorage.getEntityById(id).exists());
        Mockito.verifyNoMoreInteractions(entity);
    }
}
