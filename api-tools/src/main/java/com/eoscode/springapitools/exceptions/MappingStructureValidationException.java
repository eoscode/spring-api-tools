package com.eoscode.springapitools.exceptions;

public class MappingStructureValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MappingStructureValidationException(String message) {
        super(message);
    }

    public MappingStructureValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
