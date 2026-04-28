package com.internship.tool.exception;

import com.internship.tool.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Tool-53 — Global Exception Handler (Day 8)
 *
 * Catches ALL exceptions and returns consistent JSON error body.
 * Every error goes through here — no raw exceptions reach the client.
 *
 * SECURITY NOTES:
 * - Stack traces NEVER sent to client — logged internally only
 * - Generic 500 message shown for unexpected errors — no internal details leaked
 * - 401 and 403 handled separately — never reveal auth internals
 * - Validation errors show field-level messages — safe, no internals
 * - DB errors caught by generic handler — never expose SQL to client
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 404 — Resource Not Found ──────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(404)
                        .error("Not Found")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 400 — Validation Exception (service layer) ────────────────────────────

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex,
            HttpServletRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(400)
                        .error("Bad Request")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 400 — @Valid annotation failures (controller layer) ───────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Collect all field errors into one message
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Request validation failed: {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(400)
                        .error("Validation Failed")
                        .message(message)
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 409 — Duplicate Resource ──────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        log.warn("Duplicate resource: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(409)
                        .error("Conflict")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 400 — Wrong path variable type (e.g. /api/controls/abc) ──────────────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(400)
                        .error("Bad Request")
                        .message("Invalid value for parameter: " + ex.getName())
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 401 — Unauthenticated ─────────────────────────────────────────────────

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        // SECURITY: never reveal why authentication failed
        log.warn("Authentication failed for path: {}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(401)
                        .error("Unauthorized")
                        .message("Authentication required")
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 403 — Forbidden ───────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        // SECURITY: never reveal what resource exists or why access was denied
        log.warn("Access denied for path: {}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(403)
                        .error("Forbidden")
                        .message("You do not have permission to perform this action")
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 503 — AI Service Unavailable ─────────────────────────────────────────

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(
            AiServiceException ex,
            HttpServletRequest request) {

        log.error("AI service error: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(503)
                        .error("Service Unavailable")
                        .message("AI service is temporarily unavailable. Please try again later.")
                        .path(request.getRequestURI())
                        .build());
    }

    // ── 500 — Catch-all for unexpected errors ─────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // SECURITY: log full stack trace internally — NEVER send to client
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .status(500)
                        .error("Internal Server Error")
                        // SECURITY: generic message only — no stack trace, no class names
                        .message("An unexpected error occurred. Please try again later.")
                        .path(request.getRequestURI())
                        .build());
    }
}
