package com.internship.tool.exception;

/**
 * Thrown when a requested resource is not found in the database.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
