package com.enit.orderservice.domaine.exception;

/**
 * Thrown when order business validation fails
 */
public class OrderValidationException extends RuntimeException {
    
    public OrderValidationException(String message) {
        super(message);
    }
    
    public OrderValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
