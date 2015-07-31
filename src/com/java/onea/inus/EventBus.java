package com.java.onea.inus;

import com.java.onea.inus.event.BlindEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public final class EventBus {

    private static final String BUS_DEFAULT_IDENTIFICATION = "default";

    private static EventBus instance;

    public static EventBus getInstance() {
        if (instance == null)
            instance = new EventBus();
        return instance;
    }

    public EventBus() {
        this(BUS_DEFAULT_IDENTIFICATION, MethodFinder.INTERFACE_EVENTS, ThreadEnforcer.ANY_THREAD);
    }

    public EventBus(String identification) {
        this(identification, MethodFinder.INTERFACE_EVENTS, ThreadEnforcer.ANY_THREAD);
    }

    public EventBus(MethodFinder methodFinder) {
        this(BUS_DEFAULT_IDENTIFICATION, methodFinder, ThreadEnforcer.ANY_THREAD);
    }

    public EventBus(ThreadEnforcer enforcer) {
        this(BUS_DEFAULT_IDENTIFICATION, MethodFinder.INTERFACE_EVENTS, enforcer);
    }

    public EventBus(MethodFinder methodFinder, ThreadEnforcer enforcer) {
        this(BUS_DEFAULT_IDENTIFICATION, methodFinder, enforcer);
    }

    public EventBus(String identification, MethodFinder methodFinder) {
        this(identification, methodFinder, ThreadEnforcer.ANY_THREAD);
    }

    public EventBus(String identification, ThreadEnforcer enforcer) {
        this(identification, MethodFinder.INTERFACE_EVENTS, enforcer);
    }

    public EventBus(String identification, MethodFinder methodFinder, ThreadEnforcer enforcer) {
        mIdentification = identification;
        mMethodFinder = methodFinder;
        mThreadEnforcer = enforcer;
    }

    private final String mIdentification;

    private final MethodFinder mMethodFinder;

    private final ThreadEnforcer mThreadEnforcer;

    private final ConcurrentMap<Class<?>, Set<MethodProducer>> producersByEvent
            = new ConcurrentHashMap<Class<?>, Set<MethodProducer>>();

    private final ConcurrentMap<Class<?>, Set<MethodSubscriber>> subscribersByEvent
            = new ConcurrentHashMap<Class<?>, Set<MethodSubscriber>>();

    private final ThreadLocal<Boolean> isProcessing = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private final ThreadLocal<ConcurrentLinkedQueue<EventWithSubscriber>> eventsToProcess =
            new ThreadLocal<ConcurrentLinkedQueue<EventWithSubscriber>>() {
                @Override
                protected ConcurrentLinkedQueue<EventWithSubscriber> initialValue() {
                    return new ConcurrentLinkedQueue<EventWithSubscriber>();
                }
            };

    public void register(Object object) {
        if (object == null)
            throw new NullPointerException("Object for register must be not null!");
        mThreadEnforcer.enforce(this);
        Map<Class<?>, Set<MethodProducer>> producersMap = mMethodFinder.findAllEventProducers(object);
        for (Map.Entry<Class<?>, Set<MethodProducer>> entry : producersMap.entrySet()) {
            Class<?> eventClass = entry.getKey();
            Set<MethodProducer> availableProducers = producersByEvent.get(eventClass);
            if (availableProducers == null) {
                Set<MethodProducer> createProducersSet = new CopyOnWriteArraySet<MethodProducer>();
                availableProducers = producersByEvent.putIfAbsent(eventClass, createProducersSet);
                if (availableProducers == null)
                    availableProducers = createProducersSet;
            }
            for (MethodProducer foundedProducer : entry.getValue())
                availableProducers.add(foundedProducer);
        }
        Map<Class<?>, Set<MethodSubscriber>> subscribersMap = mMethodFinder.findAllEventSubscribers(object);
        for (Map.Entry<Class<?>, Set<MethodSubscriber>> entry : subscribersMap.entrySet()) {
            Class<?> eventClass = entry.getKey();
            Set<MethodSubscriber> availableSubscribers = subscribersByEvent.get(eventClass);
            if (availableSubscribers == null) {
                Set<MethodSubscriber> createSubscriberSet = new CopyOnWriteArraySet<MethodSubscriber>();
                availableSubscribers = subscribersByEvent.putIfAbsent(eventClass, createSubscriberSet);
                if (availableSubscribers == null)
                    availableSubscribers = createSubscriberSet;
            }
            for (MethodSubscriber foundedSubscriber : entry.getValue())
                availableSubscribers.add(foundedSubscriber);
        }
    }

    public void unregister(Object object) {
        if (object == null)
            throw new NullPointerException("Object for register must be not null!");
        mThreadEnforcer.enforce(this);
        Map<Class<?>, Set<MethodProducer>> producersMap = mMethodFinder.findAllEventProducers(object);
        for (Map.Entry<Class<?>, Set<MethodProducer>> entry : producersMap.entrySet()) {
            Class<?> eventClass = entry.getKey();
            Set<MethodProducer> availableProducers = producersByEvent.get(eventClass);
            for (MethodProducer methodProducer : availableProducers) {
                if (methodProducer.isProducerFrom(object))
                    availableProducers.remove(methodProducer);
            }
        }
        Map<Class<?>, Set<MethodSubscriber>> subscribersMap = mMethodFinder.findAllEventSubscribers(object);
        for (Map.Entry<Class<?>, Set<MethodSubscriber>> entry : subscribersMap.entrySet()) {
            Class<?> eventClass = entry.getKey();
            Set<MethodSubscriber> availableSubscribers = subscribersByEvent.get(eventClass);
            for (MethodSubscriber methodSubscriber : availableSubscribers) {
                if (methodSubscriber.isSubscriberFrom(object))
                    availableSubscribers.remove(methodSubscriber);
            }
        }
    }

    public void post(Object event) {
        if (event == null)
            throw new NullPointerException("Object for register must be not null!");
        mThreadEnforcer.enforce(this);
        Set<MethodSubscriber> methodSubscribers = subscribersByEvent.get(event.getClass());
        if (methodSubscribers != null && methodSubscribers.size() != 0) {
            for (MethodSubscriber methodSubscriber : methodSubscribers)
                enqueueEvent(event, methodSubscriber);
        }
        Set<MethodSubscriber> blindEventSubscribers;
        if ((blindEventSubscribers = subscribersByEvent.get(BlindEvent.class)) != null) {
            for (MethodSubscriber methodSubscriber : blindEventSubscribers)
                enqueueEvent(event, methodSubscriber);
        }
        processEvents();
    }

    private void enqueueEvent(Object event, MethodSubscriber methodSubscriber) {
        eventsToProcess.get().offer(new EventWithSubscriber(event, methodSubscriber));
    }

    private void processEvents() {
        if (isProcessing.get()) {
            return;
        }
        isProcessing.set(true);
        try {
            while (true) {
                EventWithSubscriber eventWithSubscriber = eventsToProcess.get().poll();
                if (eventWithSubscriber == null)
                    break;
                invokeSubscriberMethod(eventWithSubscriber.mEvent, eventWithSubscriber.mSubscriber);
            }
        } finally {
            isProcessing.set(false);
        }
    }

    private void invokeSubscriberMethod(Object event, MethodSubscriber subscriber) {
        try {
            subscriber.handleEvent(event);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(String.format(
                    "Could not dispatch event: "
                            + "%S to subscriber %s", event.getClass(), subscriber), e);
        }
    }

    @Override
    public String toString() {
        return String.format("[EventBus |%s|]", mIdentification);
    }

    private static final class EventWithSubscriber {

        private final Object mEvent;

        private final MethodSubscriber mSubscriber;

        private EventWithSubscriber(Object event, MethodSubscriber subscriber) {
            mEvent = event;
            mSubscriber = subscriber;
        }
    }

}
