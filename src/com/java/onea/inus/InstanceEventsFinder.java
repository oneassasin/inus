package com.java.onea.inus;

import com.java.onea.inus.annotation.Produce;
import com.java.onea.inus.annotation.Subscribe;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InstanceEventsFinder implements MethodFinder {

    @Override
    public Map<Class<?>, Set<MethodProducer>> findAllEventProducers(Object listener) {
        Map<Class<?>, Set<MethodProducer>> foundProducers = new HashMap<Class<?>, Set<MethodProducer>>();
        Set<Reflector.AnnotatedMethod> methods = Reflector.
                findAllMethodsAnnotatedWith(listener.getClass(), Produce.class);
        for (Reflector.AnnotatedMethod annotatedMethod : methods) {
            Class<?> eventType = checkProduceMethod(annotatedMethod);
            if (eventType == null) {
                continue;
            }
            Set<MethodProducer> producers = new HashSet<MethodProducer>();
            foundProducers.put(eventType, producers);
            producers.add(new MethodProducer(listener, annotatedMethod.mMethod));

        }
        return foundProducers;
    }

    @Override
    public Map<Class<?>, Set<MethodSubscriber>> findAllEventSubscribers(Object listener) {
        Map<Class<?>, Set<MethodSubscriber>> foundSubscribers = new HashMap<Class<?>, Set<MethodSubscriber>>();
        Set<Reflector.AnnotatedMethod> methods = Reflector.
                findAllMethodsAnnotatedWith(listener.getClass(), Subscribe.class);
        for (Reflector.AnnotatedMethod annotatedMethod : methods) {
            Class<?> eventType = checkSubscribeMethod(annotatedMethod);
            if (eventType == null) {
                continue;
            }
            Set<MethodSubscriber> subscribers = new HashSet<MethodSubscriber>();
            foundSubscribers.put(eventType, subscribers);
            subscribers.add(new MethodSubscriber(listener, annotatedMethod.mMethod));
        }
        return foundSubscribers;
    }

    private Class<?> checkSubscribeMethod(Reflector.AnnotatedMethod annotatedMethod) {
        if (annotatedMethod.mMethod.isBridge()) {
            return null;
        }
        Class<?>[] parameterTypes = annotatedMethod.mMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method " + annotatedMethod.mMethod
                    + " has @Subscribe annotation but requires " + parameterTypes.length
                    + " arguments.  Methods must require a single argument.");
        }
        Class<?> eventType = parameterTypes[0];
        if (eventType.isInterface()) {
            throw new IllegalArgumentException("Method " + annotatedMethod.mMethod
                    + " has @Subscribe annotation on " + eventType
                    + " which is an interface.  Subscription must be on a concrete class type.");
        }
        if ((annotatedMethod.mMethod.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException("Method " + annotatedMethod.mMethod
                    + " has @Subscribe annotation on " + eventType + " but is not 'public'.");
        }
        return eventType;
    }

    private Class<?> checkProduceMethod(Reflector.AnnotatedMethod annotatedMethod) {
        if (annotatedMethod.mMethod.isBridge()) {
            return null;
        }
        Class<?>[] parameterTypes = annotatedMethod.mMethod.getParameterTypes();
        if (parameterTypes.length != 0) {
            throw new IllegalArgumentException("Method " + annotatedMethod.mMethod
                    + "has @Produce annotation but requires " + parameterTypes.length
                    + " arguments.  Methods must require zero arguments.");
        }
        if (annotatedMethod.mMethod.getReturnType() == Void.class) {
            throw new IllegalArgumentException("Method " + annotatedMethod
                    + " has a return type of void.  Must declare a non-void type.");
        }
        Class<?> eventType = annotatedMethod.mMethod.getReturnType();
        if (eventType.isInterface()) {
            throw new IllegalArgumentException("Method " + annotatedMethod.mMethod
                    + " has @Produce annotation on " + eventType
                    + " which is an interface.  Producers must return a concrete class type.");
        }
        if (eventType.equals(Void.TYPE)) {
            throw new IllegalArgumentException("Method " + annotatedMethod.mMethod
                    + " has @Produce annotation but has no return type.");
        }
        if ((annotatedMethod.mMethod.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException("Method " + annotatedMethod.mMethod
                    + " has @Produce annotation on " + eventType
                    + " but is not 'public'.");
        }
        return eventType;
    }
}

