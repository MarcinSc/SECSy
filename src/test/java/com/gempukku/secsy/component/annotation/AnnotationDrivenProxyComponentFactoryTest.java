package com.gempukku.secsy.component.annotation;

import com.gempukku.secsy.SampleComponent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AnnotationDrivenProxyComponentFactoryTest {
    private AnnotationDrivenProxyComponentFactory factory;
    private Map<String, Object> valueObject;

    @Before
    public void setup() throws NoSuchMethodException {
        factory = new AnnotationDrivenProxyComponentFactory();
        valueObject = factory.createComponentValueObject(SampleComponent.class);
    }

    @After
    public void tearDown() {
        factory.disposeComponentValueObject(valueObject);
    }

    @Test
    public void testGetComponentClass() {
        final SampleComponent component = factory.createComponent(SampleComponent.class, valueObject);
        assertEquals(SampleComponent.class, factory.getComponentClass(component));
    }

    @Test
    public void testCreateComponent() {
        final SampleComponent component = factory.createComponent(SampleComponent.class, valueObject);
        assertTrue(factory.isNewComponent(component));
    }

    @Test
    public void testGetComponent() {
        final SampleComponent component = factory.getComponent(SampleComponent.class, valueObject);
        assertFalse(factory.isNewComponent(component));
    }

    @Test
    public void storeValueInPermanentStorage() {
        final SampleComponent component = factory.createComponent(SampleComponent.class, valueObject);
        assertTrue(valueObject.isEmpty());
        component.setValue("value");
        assertTrue(valueObject.isEmpty());
        factory.saveComponent(component, valueObject);
        assertEquals(1, valueObject.size());
        assertEquals("value", valueObject.get("value"));
    }

    @Test
    public void setNullValue() {
        final SampleComponent component = factory.createComponent(SampleComponent.class, valueObject);
        assertTrue(valueObject.isEmpty());
        component.setValue("value");
        assertTrue(valueObject.isEmpty());
        factory.saveComponent(component, valueObject);

        component.setValue(null);
        assertNull(component.getValue());
        factory.saveComponent(component, valueObject);
        assertNull(valueObject.get("value"));
    }

    @Test
    public void saveResetsValuesOnComponent() {
        final SampleComponent component = factory.createComponent(SampleComponent.class, valueObject);
        component.setValue("value");
        factory.saveComponent(component, valueObject);

        valueObject.clear();
        factory.saveComponent(component, valueObject);
        assertTrue(valueObject.isEmpty());
    }

    @Test
    public void callingUndefinedMethod() {
        final SampleComponent component = factory.createComponent(SampleComponent.class, valueObject);
        try {
            component.undefinedMethod();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException exp) {
            // Expected
        }
    }

    @Test
    public void testBlackBoxVisibility() {
        final SampleComponent original = factory.createComponent(SampleComponent.class, valueObject);
        assertNull(original.getValue());

        original.setValue("value");
        assertEquals("value", original.getValue());

        final SampleComponent copy = factory.getComponent(SampleComponent.class, valueObject);
        // Should not see unsaved changes
        assertNull(copy.getValue());

        factory.saveComponent(original, valueObject);
        // Changes immediately visible
        assertEquals("value", copy.getValue());
    }

    @Test
    public void testFieldTypes() {
        final SampleComponent original = factory.createComponent(SampleComponent.class, valueObject);
        final Map<String, Class<?>> componentFields = original.getComponentFields();
        assertEquals(1, componentFields.size());
        assertEquals(String.class, componentFields.get("value"));
    }

    @Test
    public void testGetFieldValue() {
        final SampleComponent original = factory.createComponent(SampleComponent.class, valueObject);
        assertNull(original.getComponentFieldValue("value", String.class));

        original.setValue("value");

        assertEquals("value", original.getComponentFieldValue("value", String.class));
    }
}
