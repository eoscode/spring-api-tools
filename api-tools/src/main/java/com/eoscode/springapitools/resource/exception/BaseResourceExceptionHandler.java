package com.eoscode.springapitools.resource.exception;

import com.eoscode.springapitools.data.filter.SearchException;
import com.eoscode.springapitools.service.exceptions.AuthorizationException;
import com.eoscode.springapitools.service.exceptions.EntityNotFoundException;
import com.eoscode.springapitools.service.exceptions.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;

public class BaseResourceExceptionHandler {

	protected final Log log = LogFactory.getLog(this.getClass());

	protected URI getPathURI() {
		return ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
	}

	protected String getPath() {
		return ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<StandardError> objectNotFound(EntityNotFoundException e, HttpServletRequest request) {
		StandardError standardError = new StandardError(now(), HttpStatus.NOT_FOUND.value(),
				"Not found", e.getMessage(), request.getRequestURI());

		log.error("objectNotFound -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(standardError);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<StandardError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
		ValidationError validationError = new ValidationError(now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
				"Validation error", e.getMessage(), request.getRequestURI());
		for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
			validationError.addError(fieldError.getField(), fieldError.getDefaultMessage());
		}

		log.error("validation -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationError);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<StandardError> constraintViolation(ConstraintViolationException e) {
		ValidationError validationError = new ValidationError(now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
				"Validation error", e.getMessage(), getPathURI().toString());

		if (e.getConstraintViolations() != null) {
			e.getConstraintViolations().forEach(constraintViolation -> validationError.addError("", constraintViolation.getMessage()));
		}

		log.error("constraintViolation -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationError);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<StandardError> dataIntegrityViolationException(DataIntegrityViolationException e) {
		ValidationError validationError = new ValidationError(now(), HttpStatus.CONFLICT.value(),
				"Database error", e.getLocalizedMessage(), getPath());

		log.error("dataIntegrityViolationException -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(validationError);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<StandardError> validation(ValidationException e, HttpServletRequest request) {
		ValidationError validationError = new ValidationError(now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
				"Validation error", e.getMessage(), request.getRequestURI());
		log.error("validation -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationError);
	}

	@ExceptionHandler(SearchException.class)
	public ResponseEntity<StandardError> searchException(SearchException e, HttpServletRequest request) {
		ValidationError validationError = new ValidationError(now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
				"Search error", e.getMessage(), request.getRequestURI());
		log.error("searchException -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationError);
	}

	@ExceptionHandler(AuthorizationException.class)
	public ResponseEntity<StandardError> authorization(AuthorizationException e, HttpServletRequest request) {
		StandardError standardError = new StandardError(now(), HttpStatus.UNAUTHORIZED.value(),
				"Access denied", e.getMessage(), request.getRequestURI());

		log.error("authorization -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(standardError);
	}

	@ExceptionHandler({UsernameNotFoundException.class, AuthenticationException.class})
	public ResponseEntity<StandardError> objectNotFound(AuthenticationException e, HttpServletRequest request) {
		StandardError standardError = new StandardError(now(), HttpStatus.FORBIDDEN.value(),
				"Access denied", e.getMessage(), request.getRequestURI());

		log.error("objectNotFound -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(standardError);
	}

	@ExceptionHandler(MethodNotAllowedException.class)
	public ResponseEntity<StandardError> methodNotAllowed(MethodNotAllowedException e, HttpServletRequest request) {
		StandardError standardError = new StandardError(now(), HttpStatus.METHOD_NOT_ALLOWED.value(),
				"Method Not Allowed", e.getMessage(), request.getRequestURI());

		log.error("methodNotAllowed -> " + e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(standardError);
	}

	private String now() {
		return Instant.now().toString();
	}

}
