package com.gempukku.secsy.system.client;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class BiDiMultimapTest {
    private BiDiMultimap<Object, Object> bdm = new BiDiMultimap<>();

    @Test
    public void testEmpty() {
        assertEquals(0, bdm.getAllKeys().size());
        assertEquals(0, bdm.getAllValues().size());
        assertFalse(bdm.containsKey(new Object()));
        assertFalse(bdm.containsValue(new Object()));
        assertEquals(0, bdm.getKeys(new Object()).size());
        assertEquals(0, bdm.getValues(new Object()).size());
        assertEquals(0, bdm.removeAllKeys(new Object()).size());
        assertEquals(0, bdm.removeAllValues(new Object()).size());
    }

    @Test
    public void testPut() {
        Object k = new Object();
        Object v = new Object();

        bdm.put(k, v);

        final Collection<? extends Object> allKeys = bdm.getAllKeys();
        assertEquals(1, allKeys.size());
        assertTrue(allKeys.contains(k));
        final Collection<? extends Object> allValues = bdm.getAllValues();
        assertEquals(1, allValues.size());
        assertTrue(allValues.contains(v));
        
        assertTrue(bdm.containsKey(k));
        assertTrue(bdm.containsValue(v));
        assertFalse(bdm.containsKey(v));
        assertFalse(bdm.containsValue(k));

        final Collection<? extends Object> keys = bdm.getKeys(v);
        assertEquals(1, keys.size());
        assertTrue(keys.contains(k));
        final Collection<? extends Object> values = bdm.getValues(k);
        assertEquals(1, values.size());
        assertTrue(values.contains(v));
    }

    @Test
    public void testRemove() {
        Object k = new Object();
        Object v = new Object();

        bdm.put(k, v);
        bdm.remove(k, v);

        testEmpty();
    }

    @Test
    public void testRemoveAllKeys() {
        Object k = new Object();
        Object v = new Object();

        bdm.put(k, v);
        bdm.removeAllKeys(v);

        testEmpty();
    }

    @Test
    public void testRemoveAllValues() {
        Object k = new Object();
        Object v = new Object();

        bdm.put(k, v);
        bdm.removeAllValues(k);

        testEmpty();
    }
}
