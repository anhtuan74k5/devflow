package com.example.devflow.exception;

/**
 * Thrown when a business rule is violated (e.g., invalid state transition,
 * duplicate operation, constraint conflict).
 * <p>
 * Maps to HTTP 400 in GlobalExceptionHandler. Keeps validation logic
 * centralized rather than scattered across controllers.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
