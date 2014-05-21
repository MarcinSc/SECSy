package com.gempukku.secsy.entity;

import com.gempukku.secsy.SampleComponent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NullEntityRefTest {
    @Test(expected = UnsupportedOperationException.class)
    public void cantAddComponent() {
        NullEntityRef.singleton.addComponent(SampleComponent.class);
    }

    @Test
    public void getComponent() {
        assertNull(NullEntityRef.singleton.getComponent(SampleComponent.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cantSaveComponent() {
        NullEntityRef.singleton.saveComponents(Mockito.mock(SampleComponent.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cantRemoveComponent() {
        NullEntityRef.singleton.removeComponents(SampleComponent.class);
    }

    @Test
    public void listComponents() {
        assertTrue(NullEntityRef.singleton.listComponents().isEmpty());
    }

    @Test
    public void doesntExist() {
        assertFalse(NullEntityRef.singleton.exists());
    }

    @Test
    public void sentEvent() {
        NullEntityRef.singleton.send(new Object());
    }

    @Test
    public void hasComponent() {
        assertFalse(NullEntityRef.singleton.hasComponent(SampleComponent.class));
    }
}
