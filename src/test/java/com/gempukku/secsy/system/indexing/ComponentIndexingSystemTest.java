package com.gempukku.secsy.system.indexing;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.EntityRef;
import com.gempukku.secsy.Event;
import com.gempukku.secsy.SampleComponent;
import com.gempukku.secsy.SampleComponent2;
import com.gempukku.secsy.entity.event.BeforeComponentDeactivated;
import com.gempukku.secsy.entity.event.BeforeComponentRemoved;
import com.gempukku.secsy.entity.event.ComponentActivated;
import com.gempukku.secsy.entity.event.ComponentAdded;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ComponentIndexingSystemTest {
    private ComponentIndexingSystem system;
    private ComponentIndexingManager.ComponentIndex<Event> sampleComponent1Index;
    private ComponentIndexingManager.ComponentIndex<Event> sampleComponent2Index;
    private ComponentIndexingManager.ComponentIndex<Event> sampleComponentBothIndex;

    private EntityRef<Event> entity;

    @Before
    public void setup() {
        system = new ComponentIndexingSystem();
        sampleComponent1Index = system.createIndex(SampleComponent.class);
        sampleComponent2Index = system.createIndex(SampleComponent2.class);
        sampleComponentBothIndex = system.createIndex(SampleComponent.class, SampleComponent2.class);
        system.finishIndexRegistration();

        entity = Mockito.mock(EntityRef.class);
    }

    @Test
    public void eventBeforeIndexCreation() {
        ComponentIndexingSystem system = new ComponentIndexingSystem();
        try {
            system.eventReceived(entity, new ComponentAdded(Collections.<Class<? extends Component>>emptySet()));
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exp) {
            // Expected
        }
    }

    @Test(expected = IllegalStateException.class)
    public void indexRegistrationAfterInitialization() {
        system.createIndex(SampleComponent.class);
    }

    @Test
    public void emptyIndices() {
        assertDoesNotHaveEntity(sampleComponent1Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponent2Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponentBothIndex.getMatchingEntities());
    }

    @Test
    public void oneComponentAdded() {
        Mockito.when(entity.listComponents()).thenReturn(Collections.<Class<? extends Component>>singleton(SampleComponent.class));

        system.eventReceived(entity, new ComponentAdded(Collections.<Class<? extends Component>>singleton(SampleComponent.class)));

        assertHasEntity(sampleComponent1Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponent2Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponentBothIndex.getMatchingEntities());
    }

    @Test
    public void oneComponentActivated() {
        Mockito.when(entity.listComponents()).thenReturn(Collections.<Class<? extends Component>>singleton(SampleComponent.class));

        system.eventReceived(entity, new ComponentActivated(Collections.<Class<? extends Component>>singleton(SampleComponent.class)));

        assertHasEntity(sampleComponent1Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponent2Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponentBothIndex.getMatchingEntities());
    }

    @Test
    public void twoComponentsAdded() {
        Mockito.when(entity.listComponents()).thenReturn(Arrays.asList(SampleComponent.class, SampleComponent2.class));

        system.eventReceived(entity, new ComponentAdded(Arrays.asList(SampleComponent.class, SampleComponent2.class)));

        assertHasEntity(sampleComponent1Index.getMatchingEntities());
        assertHasEntity(sampleComponent2Index.getMatchingEntities());
        assertHasEntity(sampleComponentBothIndex.getMatchingEntities());
    }

    @Test
    public void twoComponentsActivated() {
        Mockito.when(entity.listComponents()).thenReturn(Arrays.asList(SampleComponent.class, SampleComponent2.class));

        system.eventReceived(entity, new ComponentActivated(Arrays.asList(SampleComponent.class, SampleComponent2.class)));

        assertHasEntity(sampleComponent1Index.getMatchingEntities());
        assertHasEntity(sampleComponent2Index.getMatchingEntities());
        assertHasEntity(sampleComponentBothIndex.getMatchingEntities());
    }

    @Test
    public void oneComponentsRemoved() {
        Mockito.when(entity.listComponents()).thenReturn(Arrays.asList(SampleComponent.class, SampleComponent2.class));

        system.eventReceived(entity, new ComponentAdded(Arrays.asList(SampleComponent.class, SampleComponent2.class)));
        system.eventReceived(entity, new BeforeComponentRemoved(Arrays.<Class<? extends Component>>asList(SampleComponent.class)));

        assertDoesNotHaveEntity(sampleComponent1Index.getMatchingEntities());
        assertHasEntity(sampleComponent2Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponentBothIndex.getMatchingEntities());
    }

    @Test
    public void oneComponentsDeactivated() {
        Mockito.when(entity.listComponents()).thenReturn(Arrays.asList(SampleComponent.class, SampleComponent2.class));

        system.eventReceived(entity, new ComponentAdded(Arrays.asList(SampleComponent.class, SampleComponent2.class)));
        system.eventReceived(entity, new BeforeComponentDeactivated(Arrays.<Class<? extends Component>>asList(SampleComponent.class)));

        assertDoesNotHaveEntity(sampleComponent1Index.getMatchingEntities());
        assertHasEntity(sampleComponent2Index.getMatchingEntities());
        assertDoesNotHaveEntity(sampleComponentBothIndex.getMatchingEntities());
    }

    @Test
    public void invokingIndexAfterDestroying() {
        system.destroyIndex(sampleComponent1Index);

        try {
            sampleComponent1Index.getMatchingEntities();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exp) {
            // Expected
        }
    }

    private void assertHasEntity(Iterator<EntityRef<Event>> iterator) {
        assertTrue(iterator.hasNext());
        assertEquals(entity, iterator.next());
        assertFalse(iterator.hasNext());
    }

    private void assertDoesNotHaveEntity(Iterator<EntityRef<Event>> iterator) {
        assertFalse(iterator.hasNext());
    }
}
