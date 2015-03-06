package com.java.onea.inus;

import java.lang.reflect.Method;

public final class MethodProducer {

    private final Object mMethodInstance;

    private final Method mMethod;

    private final int hash;

    public MethodProducer(Object methodInstance, Method method) {
        if (methodInstance == null) {
            throw new NullPointerException(String.format("MethodInstance must be not null!"));
        }
        if (method == null) {
            throw new NullPointerException(String.format("Method must be not null!"));
        }
        mMethodInstance = methodInstance;
        mMethod = method;
        int h = (mMethodInstance.hashCode() << 15) ^ 0xFFFFCD7D;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 16);
        h += (h << 2) + (h << 14);
        hash = h ^ (h >>> 16);
    }

    public boolean isProducerFrom(Object object) {
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
        if (this.getClass() == o.getClass())
            return true;
        final MethodProducer methodProducer = (MethodProducer) o;
        return methodProducer.mMethod == this.mMethod
                && methodProducer.mMethodInstance == this.mMethodInstance;
    }

    @Override
    public String toString() {
        return String.format("[MethodProducer |%S|%S|]", mMethod, hash);
    }

}
