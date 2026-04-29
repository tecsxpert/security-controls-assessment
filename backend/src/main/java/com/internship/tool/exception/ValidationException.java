package com.internship.tool.exception;

/**
 * Thrown when input validation fails in the service layer.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
