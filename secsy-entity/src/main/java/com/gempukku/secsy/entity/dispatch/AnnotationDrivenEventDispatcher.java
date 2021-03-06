package com.gempukku.secsy.entity.dispatch;

import com.gempukku.secsy.context.SystemContext;
import com.gempukku.secsy.context.annotation.Inject;
import com.gempukku.secsy.context.annotation.RegisterSystem;
import com.gempukku.secsy.context.system.AbstractLifeCycleSystem;
import com.gempukku.secsy.context.util.Prioritable;
import com.gempukku.secsy.context.util.PriorityCollection;
import com.gempukku.secsy.entity.Component;
import com.gempukku.secsy.entity.EntityEventListener;
import com.gempukku.secsy.entity.EntityRef;
import com.gempukku.secsy.entity.InternalEntityManager;
import com.gempukku.secsy.entity.event.ConsumableEvent;
import com.gempukku.secsy.entity.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RegisterSystem(
        profiles = "annotationEventDispatcher"
)
public class AnnotationDrivenEventDispatcher extends AbstractLifeCycleSystem implements EntityEventListener {
    private static final Logger logger = Logger.getLogger(AnnotationDrivenEventDispatcher.class.getName());
    @Inject
    private InternalEntityManager internalEntityManager;
    @Inject(optional = true)
    private PriorityResolver priorityResolver;
    @Inject
    private SystemContext systemContext;

    private Map<Class<? extends Event>, PriorityCollection<EventListenerDefinition>> eventListenerDefinitions = new HashMap<Class<? extends Event>, PriorityCollection<EventListenerDefinition>>();

    @Override
    public float getPriority() {
        return 200;
    }

    @Override
    public void initialize() {
        internalEntityManager.addEntityEventListener(this);
        for (Object system : systemContext.getSystems()) {
            scanSystem(system);
        }
    }

    private void scanSystem(Object system) {
        for (Method method : system.getClass().getDeclaredMethods()) {
            final ReceiveEvent receiveEventAnnotation = method.getAnnotation(ReceiveEvent.class);
            if (receiveEventAnnotation != null) {
                if (method.getReturnType().equals(Void.TYPE)
                        && Modifier.isPublic(method.getModifiers())) {
                    final Class<?>[] parameters = method.getParameterTypes();
                    if (parameters.length >= 2) {
                        if (Event.class.isAssignableFrom(parameters[0])
                                && EntityRef.class.isAssignableFrom(parameters[1])) {
                            boolean valid = true;
                            for (int i = 2; i < parameters.length; i++) {
                                if (!Component.class.isAssignableFrom(parameters[i])) {
                                    valid = false;
                                    break;
                                }
                            }

                            if (valid) {
                                Class<? extends Component>[] components = new Class[parameters.length - 2];
                                for (int i = 2; i < parameters.length; i++) {
                                    components[i - 2] = (Class<? extends Component>) parameters[i];
                                }

                                addListenerDefinition((Class<? extends Event>) parameters[0],
                                        new EventListenerDefinition(system, method, components, getPriority(receiveEventAnnotation)));
                            }
                        }
                    }
                }
            }
        }
    }

    private float getPriority(ReceiveEvent receiveEventAnnotation) {
        if (priorityResolver != null && !receiveEventAnnotation.priorityName().equals("")) {
            Float priority = priorityResolver.getPriority(receiveEventAnnotation.priorityName());
            if (priority != null)
                return priority;
        }
        return receiveEventAnnotation.priority();
    }

    private void addListenerDefinition(Class<? extends Event> clazz, EventListenerDefinition eventListenerDefinition) {
        PriorityCollection<EventListenerDefinition> eventListenerDefinitions = this.eventListenerDefinitions.get(clazz);
        if (eventListenerDefinitions == null) {
            eventListenerDefinitions = new PriorityCollection<EventListenerDefinition>();
            this.eventListenerDefinitions.put(clazz, eventListenerDefinitions);
        }
        eventListenerDefinitions.add(eventListenerDefinition);
    }

    @Override
    public void eventSent(EntityRef entity, Event event) {
        ConsumableEvent consumableEvent = null;
        if (event instanceof ConsumableEvent)
            consumableEvent = (ConsumableEvent) event;
        PriorityCollection<EventListenerDefinition> eventListenerDefinitions = this.eventListenerDefinitions.get(event.getClass());
        if (eventListenerDefinitions != null) {
            for (EventListenerDefinition eventListenerDefinition : eventListenerDefinitions) {
                boolean valid = true;
                for (Class<? extends Component> componentRequired : eventListenerDefinition.getComponentParameters()) {
                    if (!entity.hasComponent(componentRequired)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    eventListenerDefinition.eventReceived(entity, event);
                    if (consumableEvent != null && consumableEvent.isConsumed())
                        break;
                }
            }
        }
    }

    private class EventListenerDefinition implements Prioritable {
        private Object system;
        private Method method;
        private Class<? extends Component>[] componentParameters;
        private float priority;

        private EventListenerDefinition(Object system, Method method, Class<? extends Component>[] componentParameters, float priority) {
            this.system = system;
            this.method = method;
            this.componentParameters = componentParameters;
            this.priority = priority;
        }

        @Override
        public float getPriority() {
            return priority;
        }

        public Class<? extends Component>[] getComponentParameters() {
            return componentParameters;
        }

        public void eventReceived(EntityRef entity, Event event) {
            Object[] params = new Object[2 + componentParameters.length];
            params[0] = event;
            params[1] = entity;
            int index = 2;
            for (Class<? extends Component> componentParameter : componentParameters) {
                params[index++] = entity.getComponent(componentParameter);
            }

            long start = System.currentTimeMillis();
            try {
                method.invoke(system, params);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                long time = System.currentTimeMillis() - start;
                String message = time + "ms - " + method.getDeclaringClass().getSimpleName() + ":" + method.getName();
                if (time > 100) {
                    logger.severe(message);
                } else if (time > 50) {
                    logger.warning(message);
                } else {
                    logger.fine(message);
                }
            }
        }
    }
}
