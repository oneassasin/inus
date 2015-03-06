package com.java.onea.inus;

import java.util.Map;
import java.util.Set;

public interface MethodFinder {

    // Class<?> => Producing event
    public Map<Class<?>, Set<MethodProducer>> findAllEventProducers(Object listener);

    // Class<?> => Subscribe event
    public Map<Class<?>, Set<MethodSubscriber>> findAllEventSubscribers(Object listener);

    public static MethodFinder INTERFACE_EVENTS = new InterfaceEventsFinder();

    public static MethodFinder INSTANCE_EVENTS = new InstanceEventsFinder();

}
