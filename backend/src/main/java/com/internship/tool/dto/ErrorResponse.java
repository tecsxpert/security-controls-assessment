package com.internship.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Standard error response body.
 * Returned by GlobalExceptionHandler for ALL errors.
 *
 * SECURITY NOTE:
 * - Never expose stack traces, internal class names, or DB error messages
 * - 'message' is always a safe, human-readable string
 * - 'details' only shown in non-production profiles
 * {
 *   "success": false,
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Security control not found with id: 99",
 *   "path": "/api/controls/99",
 *   "timestamp": "2026-04-23T10:00:00"
 * }
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
