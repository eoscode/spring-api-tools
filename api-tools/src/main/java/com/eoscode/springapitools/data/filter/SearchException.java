package com.eoscode.springapitools.data.filter;

public class SearchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }

}
