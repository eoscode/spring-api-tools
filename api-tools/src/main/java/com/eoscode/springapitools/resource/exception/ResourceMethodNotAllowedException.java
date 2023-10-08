package com.eoscode.springapitools.resource.exception;

public class ResourceMethodNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceMethodNotAllowedException(String message) {
        super(message);
    }

    public ResourceMethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

}
