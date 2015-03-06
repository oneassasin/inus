package com.java.onea.inus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public final class Reflector {

    private Reflector() {
    }

    public static Set<AnnotatedMethod> findAllMethodsAnnotatedWith(Class<?> object, Class<? extends Annotation> annotation) {
        Set<AnnotatedMethod> foundMethods = new HashSet<AnnotatedMethod>();
        for (Method method : object.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                foundMethods.add(new AnnotatedMethod(method, method.getAnnotation(annotation)));
            }
        }
        return foundMethods;
    }

    public static class AnnotatedMethod {

        public final Method mMethod;

        public final Annotation mAnnotation;

        public AnnotatedMethod(Method method, Annotation annotation) {
            mMethod = method;
            mAnnotation = annotation;
        }

    }

}
