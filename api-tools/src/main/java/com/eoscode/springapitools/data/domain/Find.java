package com.eoscode.springapitools.data.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Find {
    String[] ignoreAttributes() default {};
    boolean ignoreNoDeleteAnnotation() default false;
}
