package com.gempukku.secsy.system.client;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimpleObjectCloudTest {
    private SimpleObjectCloud<Object> cloud = new SimpleObjectCloud<>();

    @Test
    public void queryingEmpty() {
        assertFalse(cloud.containsEntity(new Object()));
        assertFalse(cloud.isRootEntity(new Object()));
        assertTrue(cloud.getAllEntities().isEmpty());
        assertTrue(cloud.getRootEntities().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addingNonRootEntity() {
        cloud.setEntityState(false, new Object(), Collections.<Object>emptySet());
    }

    @Test
    public void addingRootEntity() {
        final Object root = new Object();

        final Collection<Object> toRemove = cloud.setEntityState(true, root, Collections.<Object>emptySet());
        assertEquals(0, toRemove.size());

        final Collection<Object> allEntities = cloud.getAllEntities();
        assertEquals(1, allEntities.size());
        assertTrue(allEntities.contains(root));

        final Collection<Object> rootEntities = cloud.getRootEntities();
        assertEquals(1, rootEntities.size());
        assertTrue(rootEntities.contains(root));

        assertTrue(cloud.isRootEntity(root));
        assertTrue(cloud.containsEntity(root));
    }

    @Test
    public void addingRootWithDependency() {
        final Object root = new Object();
        final Object dependency = new Object();

        final Collection<Object> toRemove = cloud.setEntityState(true, root, Collections.singleton(dependency));
        assertEquals(0, toRemove.size());

        final Collection<Object> allEntities = cloud.getAllEntities();
        assertEquals(2, allEntities.size());
        assertTrue(allEntities.contains(root));
        assertTrue(allEntities.contains(dependency));

        final Collection<Object> rootEntities = cloud.getRootEntities();
        assertEquals(1, rootEntities.size());
        assertTrue(rootEntities.contains(root));

        assertTrue(cloud.isRootEntity(root));
        assertTrue(cloud.containsEntity(root));

        assertFalse(cloud.isRootEntity(dependency));
        assertTrue(cloud.containsEntity(dependency));
    }

    @Test
    public void addingRootWithDependencyWithDependency() {
        final Object root = new Object();
        final Object dependency1 = new Object();
        final Object dependency2 = new Object();

        final Collection<Object> toRemove1 = cloud.setEntityState(true, root, Collections.singleton(dependency1));
        assertEquals(0, toRemove1.size());
        final Collection<Object> toRemove2 = cloud.setEntityState(false, dependency1, Collections.singleton(dependency2));
        assertEquals(0, toRemove2.size());

        final Collection<Object> allEntities = cloud.getAllEntities();
        assertEquals(3, allEntities.size());
        assertTrue(allEntities.contains(root));
        assertTrue(allEntities.contains(dependency1));
        assertTrue(allEntities.contains(dependency2));

        final Collection<Object> rootEntities = cloud.getRootEntities();
        assertEquals(1, rootEntities.size());
        assertTrue(rootEntities.contains(root));

        assertTrue(cloud.isRootEntity(root));
        assertTrue(cloud.containsEntity(root));

        assertFalse(cloud.isRootEntity(dependency1));
        assertTrue(cloud.containsEntity(dependency1));

        assertFalse(cloud.isRootEntity(dependency2));
        assertTrue(cloud.containsEntity(dependency2));
    }

    @Test
    public void replacingDependencyForRoot() {
        final Object root = new Object();
        final Object dependency1 = new Object();
        final Object dependency2 = new Object();

        final Collection<Object> toRemove1 = cloud.setEntityState(true, root, Collections.singleton(dependency1));
        assertEquals(0, toRemove1.size());
        final Collection<Object> toRemove2 = cloud.setEntityState(true, root, Collections.singleton(dependency2));
        assertEquals(1, toRemove2.size());
        assertTrue(toRemove2.contains(dependency1));

        final Collection<Object> allEntities = cloud.getAllEntities();
        assertEquals(2, allEntities.size());
        assertTrue(allEntities.contains(root));
        assertTrue(allEntities.contains(dependency2));

        final Collection<Object> rootEntities = cloud.getRootEntities();
        assertEquals(1, rootEntities.size());
        assertTrue(rootEntities.contains(root));

        assertTrue(cloud.isRootEntity(root));
        assertTrue(cloud.containsEntity(root));

        assertFalse(cloud.isRootEntity(dependency1));
        assertFalse(cloud.containsEntity(dependency1));

        assertFalse(cloud.isRootEntity(dependency2));
        assertTrue(cloud.containsEntity(dependency2));
    }

    @Test
    public void changingStateOfRoot() {
        final Object root = new Object();
        final Object dependency = new Object();

        cloud.setEntityState(true, root, Collections.<Object>emptySet());
        final Collection<Object> toRemove = cloud.setEntityState(false, root, Collections.singleton(dependency));
        assertEquals(1, toRemove.size());
        assertTrue(toRemove.contains(root));

        assertFalse(cloud.containsEntity(root));
        assertFalse(cloud.containsEntity(dependency));
        assertFalse(cloud.isRootEntity(root));
        assertFalse(cloud.isRootEntity(dependency));
        assertTrue(cloud.getAllEntities().isEmpty());
        assertTrue(cloud.getRootEntities().isEmpty());
    }

    @Test
    public void changingStateOfRootThatIsDependencyOfAnother() {
        final Object root1 = new Object();
        final Object root2 = new Object();

        cloud.setEntityState(true, root1, Collections.<Object>emptySet());
        cloud.setEntityState(true, root2, Collections.singleton(root1));

        Collection<Object> allEntities = cloud.getAllEntities();
        assertEquals(2, allEntities.size());
        assertTrue(allEntities.contains(root1));
        assertTrue(allEntities.contains(root2));

        Collection<Object> rootEntities = cloud.getRootEntities();
        assertEquals(2, rootEntities.size());
        assertTrue(rootEntities.contains(root1));
        assertTrue(rootEntities.contains(root2));

        assertTrue(cloud.isRootEntity(root1));
        assertTrue(cloud.isRootEntity(root2));
        assertTrue(cloud.containsEntity(root1));
        assertTrue(cloud.containsEntity(root2));

        final Collection<Object> toRemove = cloud.setEntityState(false, root1, Collections.<Object>emptySet());
        assertEquals(0, toRemove.size());


        allEntities = cloud.getAllEntities();
        assertEquals(2, allEntities.size());
        assertTrue(allEntities.contains(root1));
        assertTrue(allEntities.contains(root2));

        rootEntities = cloud.getRootEntities();
        assertEquals(1, rootEntities.size());
        assertTrue(rootEntities.contains(root2));

        assertFalse(cloud.isRootEntity(root1));
        assertTrue(cloud.isRootEntity(root2));
        assertTrue(cloud.containsEntity(root1));
        assertTrue(cloud.containsEntity(root2));
    }
}
