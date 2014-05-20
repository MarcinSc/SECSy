package com.gempukku.secsy.component.annotation;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.component.ComponentFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnnotationDrivenProxyComponentFactory implements ComponentFactory<Map<String, Object>> {
    private static final Object NULL_VALUE = new Object();
    private Method componentClassMethod;
    private Method componentFieldsMethod;
    private Method componentFieldValueMethod;
    private Map<Class<? extends Component>, ComponentDef> componentDefinitions = new HashMap<>();

    public AnnotationDrivenProxyComponentFactory() throws NoSuchMethodException {
        componentClassMethod = Component.class.getMethod("getComponentClass", new Class<?>[0]);
        componentFieldsMethod = Component.class.getMethod("getComponentFields", new Class<?>[0]);
        componentFieldValueMethod = Component.class.getMethod("getComponentFieldValue", new Class<?>[] {String.class, Class.class});
    }

    @Override
    public <T extends Component> Map<String, Object> createComponentValueObject(Class<T> clazz) {
        if (componentDefinitions.get(clazz) == null) {
            componentDefinitions.put(clazz, new ComponentDef(clazz));
        }
        return new HashMap<>();
    }

    @Override
    public <T extends Component> void disposeComponentValueObject(Map<String, Object> valueObject) {
        // Nothing to do here
    }


    @Override
    public <T extends Component> T createComponent(Class<T> clazz, Map<String, Object> valueObject) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ComponentView(clazz, valueObject, true));
    }

    @Override
    public <T extends Component> T getComponent(Class<T> clazz, Map<String, Object> valueObject) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ComponentView(clazz, valueObject, false));
    }

    @Override
    public <T extends Component> void saveComponent(T component, Map<String, Object> valueObject) {
        final ComponentView componentView = (ComponentView) Proxy.getInvocationHandler(component);
        for (Map.Entry<String, Object> change : componentView.changes.entrySet()) {
            final Object newValue = change.getValue();
            if (newValue == NULL_VALUE) {
                valueObject.remove(change.getKey());
            } else {
                valueObject.put(change.getKey(), newValue);
            }
        }
        componentView.changes.clear();
    }

    @Override
    public <T extends Component> boolean isNewComponent(T component) {
        return ((ComponentView) Proxy.getInvocationHandler(component)).isNewComponent();
    }

    private class ComponentDef {
        private Map<String, Class<?>> fieldTypes = new HashMap<>();

        private ComponentDef(Class<? extends Component> clazz) {
            for (Method method : clazz.getDeclaredMethods()) {
                final GetProperty get = method.getAnnotation(GetProperty.class);
                if (get != null) {
                    final String fieldName = get.value();
                    final Class<?> fieldType = method.getReturnType();

                    final Class<?> existingType = fieldTypes.get(fieldName);
                    if (existingType != null) {
                        if (existingType != fieldType) {
                            throw new IllegalStateException("Invalid component definition, field " + fieldName + " uses different value types");
                        }
                    } else {
                        fieldTypes.put(fieldName, fieldType);
                    }
                }

                final SetProperty set = method.getAnnotation(SetProperty.class);
                if (set != null) {
                    final String fieldName = set.value();
                    final Class<?> fieldType = method.getParameterTypes()[0];

                    final Class<?> existingType = fieldTypes.get(fieldName);
                    if (existingType != null) {
                        if (existingType != fieldType) {
                            throw new IllegalStateException("Invalid component definition, field " + fieldName + " uses different value types");
                        }
                    } else {
                        fieldTypes.put(fieldName, fieldType);
                    }
                }
            }
        }

        private Map<String, Class<?>> getFieldTypes() {
            return Collections.unmodifiableMap(fieldTypes);
        }
    }

    private class ComponentView implements InvocationHandler {
        private Class<? extends Component> clazz;
        private Map<String, Object> storedValues;
        private Map<String, Object> changes = new HashMap<>();
        private boolean newComponent;

        private ComponentView(Class<? extends Component> clazz, Map<String, Object> storedValues, boolean newComponent) {
            this.clazz = clazz;
            this.storedValues = storedValues;
            this.newComponent = newComponent;
        }

        public boolean isNewComponent() {
            return newComponent;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(componentClassMethod)) {
                return clazz;
            }
            if (method.equals(componentFieldsMethod)) {
                return componentDefinitions.get(clazz).getFieldTypes();
            }
            if (method.equals(componentFieldValueMethod)) {
                return handleGet((String) args[0]);
            }

            final GetProperty get = method.getAnnotation(GetProperty.class);
            if (get != null) {
                return handleGet(get.value());
            }
            final SetProperty set = method.getAnnotation(SetProperty.class);
            if (set != null) {
                return handleSet(args[0], set.value());
            }
            throw new UnsupportedOperationException("Component method invoked without property defined");
        }

        private Object handleSet(Object arg, String fieldName) {
            if (arg == null) {
                arg = NULL_VALUE;
            }
            changes.put(fieldName, arg);

            return null;
        }

        private Object handleGet(String fieldName) {
            final Object changedValue = changes.get(fieldName);
            if (changedValue != null) {
                if (changedValue == NULL_VALUE) {
                    return null;
                } else {
                    return changedValue;
                }
            } else {
                return storedValues.get(fieldName);
            }
        }
    }
}
