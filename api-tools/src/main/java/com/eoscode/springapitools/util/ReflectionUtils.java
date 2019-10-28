package com.eoscode.springapitools.util;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static Object getObject(Field field, String value) {
        if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
            return Integer.parseInt(value);
        } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
            return Integer.parseInt(value);
        } else if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else {
            throw new IllegalArgumentException("value type not valid");
        }
    }

}
