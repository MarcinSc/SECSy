package com.gempukku.secsy.entity.component;

import com.gempukku.secsy.context.system.ShareSystemInitializer;
import com.gempukku.secsy.entity.SampleComponent;
import com.gempukku.secsy.entity.SimpleComponentFieldConverter;
import com.gempukku.secsy.entity.SimpleEntityComponentFieldHandler;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MapNamingConventionProxyComponentManagerTest {
    private MapNamingConventionProxyComponentManager factory;

    @Before
    public void setup() throws NoSuchMethodException {
        factory = new MapNamingConventionProxyComponentManager();
        ShareSystemInitializer shareSystemInitializer = new ShareSystemInitializer();

        Map<Class<?>, Object> systems = new HashMap<Class<?>, Object>();
        systems.put(EntityComponentFieldHandler.class, new SimpleEntityComponentFieldHandler());
        systems.put(ComponentFieldConverter.class, new SimpleComponentFieldConverter());

        shareSystemInitializer.initializeObjects(Arrays.<Object>asList(factory), systems);
    }

    @Test
    public void testGetComponentClass() {
        final SampleComponent component = factory.createComponent(null, SampleComponent.class);
        assertEquals(SampleComponent.class, factory.getComponentClass(component));
    }

    @Test
    public void storeValueInPermanentStorage() {
        final SampleComponent component = factory.createComponent(null, SampleComponent.class);
        component.setValue("value");
        assertEquals("value", component.getValue());
        factory.saveComponent(component, component);
        assertEquals("value", factory.getComponentFieldValue(component, "value", String.class));
    }

    @Test
    public void setNullValue() {
        final SampleComponent component = factory.createComponent(null, SampleComponent.class);
        component.setValue("value");
        factory.saveComponent(component, component);

        component.setValue(null);
        assertNull(component.getValue());
        factory.saveComponent(component, component);
        assertNull(factory.getComponentFieldValue(component, "value", String.class));
    }
}
