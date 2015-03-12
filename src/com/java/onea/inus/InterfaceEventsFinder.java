package com.java.onea.inus;

import com.java.onea.inus.annotation.Produce;
import com.java.onea.inus.annotation.Subscribe;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InterfaceEventsFinder implements MethodFinder {

    @Override
    public Map<Class<?>, Set<MethodProducer>> findAllEventProducers(Object listener) {
        Map<Class<?>, Set<MethodProducer>> foundProducers = new HashMap<Class<?>, Set<MethodProducer>>();
        Class<?> listenerClass = listener.getClass();
        String listenerClassName = listenerClass.getName();
        while (!listenerClassName.startsWith("java")
                && !listenerClassName.startsWith("android")) {
            Class<?>[] interfacesListenerClass = listenerClass.getInterfaces();
            for (Class<?> interfaceListenerClass : interfacesListenerClass) {
                Map<Method, Set<Annotation>> methods = Reflector.findAllMethodsAnnotatedWith(interfaceListenerClass, Produce.class);
                for (Map.Entry<Method, Set<Annotation>> method : methods.entrySet()) {
                    Class<?> eventType = checkProduceMethod(method.getKey());
                    if (eventType == null)
                        continue;
                    Set<MethodProducer> producers = foundProducers.get(eventType);
                    if (producers == null) {
                        producers = new HashSet<MethodProducer>();
                        foundProducers.put(eventType, producers);
                    }
                    producers.add(new MethodProducer(listener, method.getKey()));
                }
            }
            listenerClass = listenerClass.getSuperclass();
            listenerClassName = listenerClass.getName();
        }
        return foundProducers;
    }

    @Override
    public Map<Class<?>, Set<MethodSubscriber>> findAllEventSubscribers(Object listener) {
        Map<Class<?>, Set<MethodSubscriber>> foundSubscriber = new HashMap<Class<?>, Set<MethodSubscriber>>();
        Class<?> listenerClass = listener.getClass();
        String listenerClassName = listenerClass.getName();
        while (!listenerClassName.startsWith("java")
                && !listenerClassName.startsWith("android")) {
            Class<?>[] interfacesListenerClass = listenerClass.getInterfaces();
            for (Class<?> interfaceListenerClass : interfacesListenerClass) {
                Map<Method, Set<Annotation>> methods = Reflector.findAllMethodsAnnotatedWith(interfaceListenerClass, Subscribe.class);
                for (Map.Entry<Method, Set<Annotation>> method : methods.entrySet()) {
                    Class<?> eventType = checkSubscribeMethod(method.getKey());
                    if (eventType == null)
                        continue;
                    Set<MethodSubscriber> subscribers = foundSubscriber.get(eventType);
                    if (subscribers == null) {
                        subscribers = new HashSet<MethodSubscriber>();
                        foundSubscriber.put(eventType, subscribers);
                    }
                    subscribers.add(new MethodSubscriber(listener, method.getKey()));
                }
            }
            listenerClass = listenerClass.getSuperclass();
            listenerClassName = listenerClass.getName();
        }
        return foundSubscriber;
    }

    private Class<?> checkSubscribeMethod(Method method) {
        if (method.isBridge())
            return null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method " + method
                    + " has @Subscribe annotation but requires " + parameterTypes.length
                    + " arguments.  Methods must require a single argument.");
        }
        Class<?> eventType = parameterTypes[0];
        if (eventType.isInterface()) {
            throw new IllegalArgumentException("Method " + method
                    + " has @Subscribe annotation on " + eventType
                    + " which is an interface.  Subscription must be on a concrete class type.");
        }
        if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException("Method " + method
                    + " has @Subscribe annotation on " + eventType + " but is not 'public'.");
        }
        return eventType;
    }

    private Class<?> checkProduceMethod(Method method) {
        if (method.isBridge()) {
            return null;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 0) {
            throw new IllegalArgumentException("Method " + method
                    + "has @Produce annotation but requires " + parameterTypes.length
                    + " arguments.  Methods must require zero arguments.");
        }
        if (method.getReturnType() == Void.class) {
            throw new IllegalArgumentException("Method " + method
                    + " has a return type of void.  Must declare a non-void type.");
        }
        Class<?> eventType = method.getReturnType();
        if (eventType.isInterface()) {
            throw new IllegalArgumentException("Method " + method
                    + " has @Produce annotation on " + eventType
                    + " which is an interface.  Producers must return a concrete class type.");
        }
        if (eventType.equals(Void.TYPE)) {
            throw new IllegalArgumentException("Method " + method
                    + " has @Produce annotation but has no return type.");
        }
        if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException("Method " + method
                    + " has @Produce annotation on " + eventType
                    + " but is not 'public'.");
        }
        return eventType;
    }

}
