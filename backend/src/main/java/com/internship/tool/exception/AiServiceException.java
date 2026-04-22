package com.internship.tool.exception;

/**
 * Thrown when the AI microservice call fails.
 * Handled gracefully — never returns HTTP 500 to client.
 * Maps to HTTP 503 Service Unavailable.
 */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
