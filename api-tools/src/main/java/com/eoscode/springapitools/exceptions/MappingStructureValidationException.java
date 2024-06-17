package com.eoscode.springapitools.exceptions;

import java.io.Serial;

public class MappingStructureValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MappingStructureValidationException(String message) {
        super(message);
    }

    public MappingStructureValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
