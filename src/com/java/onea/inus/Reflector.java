package com.java.onea.inus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public final class Reflector {

    private Reflector() {
    }

    public static Map<Method, Set<Annotation>> getAllAnnotatedMethods(Class<?> object) {
        Map<Method, Set<Annotation>> foundMethods = new HashMap<Method, Set<Annotation>>();
        for (Method method : object.getMethods()) {
            if (method.isBridge())
                continue;
            Annotation[] methodAnnotations = method.getAnnotations();
            if (methodAnnotations.length == 0)
                continue;
            Set<Annotation> methodAnnotationSet = new HashSet<Annotation>();
            Collections.addAll(methodAnnotationSet, methodAnnotations);
            foundMethods.put(method, methodAnnotationSet);
        }
        return foundMethods;
    }


    public static Map<Method, Set<Annotation>> findAllMethodsAnnotatedWith(Class<?> object, Class<? extends Annotation> annotation) {
        Map<Method, Set<Annotation>> allAnnotatedMethods = getAllAnnotatedMethods(object);
        for (Map.Entry<Method, Set<Annotation>> entry : allAnnotatedMethods.entrySet())
            if (!entry.getKey().isAnnotationPresent(annotation))
                allAnnotatedMethods.remove(entry.getKey());
        return allAnnotatedMethods;
    }

    public static Map<Field, Set<Annotation>> getAllAnnotatedFields(Class<?> object) {
        Map<Field, Set<Annotation>> foundFields = new HashMap<Field, Set<Annotation>>();
        for (Field field : object.getFields()) {
            Annotation[] fieldAnnotations = field.getAnnotations();
            if (fieldAnnotations.length == 0)
                continue;
            Set<Annotation> fieldAnnotationSet = new HashSet<Annotation>();
            Collections.addAll(fieldAnnotationSet, fieldAnnotations);
            foundFields.put(field, fieldAnnotationSet);
        }
        return foundFields;
    }

    public static Map<Field, Set<Annotation>> findAllFieldsAnnotatedWith(Class<?> object, Class<? extends Annotation> annotation) {
        Map<Field, Set<Annotation>> allAnnotatedFields = getAllAnnotatedFields(object);
        for (Map.Entry<Field, Set<Annotation>> entry : allAnnotatedFields.entrySet())
            if (!entry.getKey().isAnnotationPresent(annotation))
                allAnnotatedFields.remove(entry.getKey());
        return allAnnotatedFields;
    }

}
