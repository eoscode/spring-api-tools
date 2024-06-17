package com.eoscode.springapitools.exceptions;

import java.io.Serial;

public class AuthorizationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

}
