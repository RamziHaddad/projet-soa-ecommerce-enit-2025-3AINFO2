package com.enit.orderservice.interfaces.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response format for REST API
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationError> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }
    
    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        this(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
    
    public record ValidationError(String field, String message) {}
}
