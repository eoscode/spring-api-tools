package com.eoscode.springapitools.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class ObjectUtils {

    public static <T> T getObject(Field field, String value) {
        return getObject(field.getType(), value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObject(Class<?> type, String value) {
        if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Boolean.class || type == boolean.class) {
            if ("true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
                return (T) Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value))  {
                return (T) Boolean.FALSE;
            }
            return (T) ("true".equalsIgnoreCase(value) || ("1".equalsIgnoreCase(value)) ? Boolean.TRUE : Boolean.FALSE);
        } else if (type == Double.class || type == double.class) {
            return (T) Double.valueOf(value);
        } else if (type == BigDecimal.class) {
            return (T) BigDecimal.valueOf(Double.parseDouble(value));
        } else if (type == String.class) {
            return (T) value;
        } else if (type == Date.class) {
            if (StringUtils.isNumber(value)) {
                return (T) new Date(Long.parseLong(value));
            } else {
                Instant instant = Instant.parse(value);
                return (T) Date.from(instant);
            }
        } else if (type == UUID.class) {
            return (T) UUID.fromString(value);
        }
        return (T) value;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getObject(Class<?> type, Object value) {
        if (type == Date.class) {
            if (value.getClass() == String.class) {
                String str = value.toString();
                if (StringUtils.isNumber(str)) {
                    return (T) new Date(Long.parseLong(str));
                } else {
                    Instant instant = Instant.parse(str);
                    return (T) Date.from(instant);
                }
            } else if (value.getClass() == Long.class || value.getClass() == Integer.class) {
                return (T) new Date(Long.parseLong(value.toString()));
            }
        } else if (type != String.class && value.getClass() == String.class) {
            return getObject(type, value.toString());
        }
        return (T) value;
    }

}
