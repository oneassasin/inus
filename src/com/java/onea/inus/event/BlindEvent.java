package com.java.onea.inus.event;

import java.lang.reflect.Field;

public final class BlindEvent {

    public BlindEvent(Object _object) {
        object = _object;
    }

    private final Object object;

    @Override
    public String toString() {
        String result = "";
        for (Field field : object.getClass().getFields()) {
            try {
                result += field.get(object).toString();
            } catch (IllegalAccessException ignore) {
            }
        }
        return result;
    }

}
