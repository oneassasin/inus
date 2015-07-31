package com.java.onea.inus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MethodSubscriber {

    private final Object mMethodInstance;

    private final Method mMethod;

    private final int hash;

    public MethodSubscriber(Object methodInstance, Method method) {
        if (methodInstance == null)
            throw new NullPointerException("MethodInstance must be not null!");
        if (method == null)
            throw new NullPointerException("Method must be not null!");
        mMethodInstance = methodInstance;
        mMethod = method;
        method.setAccessible(true);
        int h = (mMethodInstance.hashCode() << 15) ^ 0xFFFFCD7D;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 16);
        h += (h << 2) + (h << 14);
        hash = h ^ (h >>> 16);
    }

    public void handleEvent(Object event) throws InvocationTargetException {
        try {
            mMethod.invoke(mMethodInstance, event);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    public boolean isSubscriberFrom(Object object) {
        return object == mMethodInstance;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (this.getClass() != o.getClass())
            return false;
        final MethodSubscriber methodSubscriber = (MethodSubscriber) o;
        return methodSubscriber.mMethod == this.mMethod
                && methodSubscriber.mMethodInstance == this.mMethodInstance;
    }

    @Override
    public String toString() {
        return String.format("[MethodSubscriber |%S|%S|]", mMethod, hash);
    }

}
