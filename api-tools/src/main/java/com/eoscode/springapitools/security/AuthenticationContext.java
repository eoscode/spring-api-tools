package com.eoscode.springapitools.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


public class AuthenticationContext {

    public static Optional<Auth<?>> authenticated() {

        try {
            return Optional.of((Auth<?>) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }
        catch (Exception e) {
            return Optional.empty();
        }

    }

}
