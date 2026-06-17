package com.example.devflow.exception;

/**
 * Thrown when an authenticated user attempts an operation on a resource
 * they do not own or are not authorized to access.
 * <p>
 * Maps to HTTP 403 in GlobalExceptionHandler. Separate from Spring Security's
 * AccessDeniedException to keep our custom exception hierarchy clean and
 * allow specific handling in the API layer.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
