package com.internship.tool.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 Handle not found
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 404
                ));
    }

    // 🔴 Handle bad request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidation(Exception ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 400
                ));
    }

    // 🔴 Catch all (prevent crash)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong"));
    }
}
