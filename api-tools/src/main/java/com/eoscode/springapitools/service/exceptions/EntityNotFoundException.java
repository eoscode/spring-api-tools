package com.eoscode.springapitools.service.exceptions;

public class EntityNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public EntityNotFoundException(String message) {
		super(message);
	}
	
	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
