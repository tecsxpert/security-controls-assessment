package com.internship.tool.exception;

/**
 * Thrown when trying to create a resource that already exists.
 * Example: duplicate controlId.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
