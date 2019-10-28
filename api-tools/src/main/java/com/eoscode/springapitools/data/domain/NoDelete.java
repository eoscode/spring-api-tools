package com.eoscode.springapitools.data.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoDelete {
    String field() default "status";
    String defaultValue() default "1";
    String deleteValue() default "0";
}
