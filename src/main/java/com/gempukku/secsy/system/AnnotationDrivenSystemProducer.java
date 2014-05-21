package com.gempukku.secsy.system;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AnnotationDrivenSystemProducer implements SystemProducer<Object> {
    private Class<? extends Annotation> annotation;

    private Set<Object> systemsDetected = new HashSet<>();

    public AnnotationDrivenSystemProducer(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    public void scanReflections(Reflections reflections) throws IllegalAccessException, InstantiationException {
        for (Class<?> type : reflections.getTypesAnnotatedWith(annotation)) {
            systemsDetected.add(type.newInstance());
        }
    }

    @Override
    public Collection<Object> createSystems() {
        return Collections.unmodifiableCollection(systemsDetected);
    }
}
