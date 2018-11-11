package com.gempukku.secsy.context.system;

import com.google.common.base.Predicate;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReflectionsAnnotatedTypesSystemProducer implements SystemProducer {
    private Class<? extends Annotation> annotation;
    private Predicate<Class<?>> classPredicate;

    private Set<Class<?>> systemsDetected = new HashSet<Class<?>>();

    public ReflectionsAnnotatedTypesSystemProducer(Class<? extends Annotation> annotation,
                                                   Predicate<Class<?>> classPredicate) {
        this.annotation = annotation;
        this.classPredicate = classPredicate;
    }

    public void scanReflections(Reflections reflections) {
        for (Class<?> type : reflections.getTypesAnnotatedWith(annotation)) {
            if (classPredicate.apply(type))
                systemsDetected.add(type);
        }
    }

    @Override
    public Iterable<Object> createSystems() {
        try {
            Set<Object> systems = new HashSet<Object>();

            for (Class<?> system : systemsDetected) {
                systems.add(system.newInstance());
            }

            return Collections.unmodifiableCollection(systems);
        } catch (IllegalAccessException exp) {
            throw new RuntimeException("Unable to instantiate systems", exp);
        } catch (InstantiationException exp) {
            throw new RuntimeException("Unable to instantiate systems", exp);
        }
    }
}
