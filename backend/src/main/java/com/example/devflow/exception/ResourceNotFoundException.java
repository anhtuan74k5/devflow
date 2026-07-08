package com.example.devflow.exception;

/**
 * Thrown when a requested resource (User, Project, Task, etc.) cannot be found.
 * <p>
 * Maps to HTTP 404 in GlobalExceptionHandler. Extends RuntimeException to avoid
 * forced try-catch in service layer, keeping business logic clean.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
