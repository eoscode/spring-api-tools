package com.eoscode.springapitools.resource.exception;

public class MethodNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MethodNotAllowedException(String message) {
        super(message);
    }

    public MethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

}
