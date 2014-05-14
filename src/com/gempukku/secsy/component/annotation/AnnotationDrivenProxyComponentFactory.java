package com.gempukku.secsy.component.annotation;

import com.gempukku.secsy.Component;
import com.gempukku.secsy.component.ComponentFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AnnotationDrivenProxyComponentFactory implements ComponentFactory<Map<String, Object>> {
    private static final Object NULL_VALUE = new Object();
    private Method componentClassMethod;

    public AnnotationDrivenProxyComponentFactory() throws NoSuchMethodException {
        componentClassMethod = Component.class.getMethod("getComponentClass", new Class<?>[0]);
    }

    @Override
    public <T extends Component> Map<String, Object> createComponentValueObject(Class<T> clazz) {
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

            final GetProperty get = method.getAnnotation(GetProperty.class);
            if (get != null) {
                return handleGet(get);
            }
            final SetProperty set = method.getAnnotation(SetProperty.class);
            if (set != null) {
                return handleSet(args[0], set);
            }
            throw new UnsupportedOperationException("Component method invoked without property defined");
        }

        private Object handleSet(Object arg, SetProperty set) {
            if (arg == null) {
                arg = NULL_VALUE;
            }
            changes.put(set.value(), arg);

            return null;
        }

        private Object handleGet(GetProperty get) {
            final String propertyName = get.value();
            final Object changedValue = changes.get(propertyName);
            if (changedValue != null) {
                if (changedValue == NULL_VALUE) {
                    return null;
                } else {
                    return changedValue;
                }
            } else {
                return storedValues.get(propertyName);
            }
        }
    }
}
