package org.com.exceptions;

public class OutboxEventCreationException extends RuntimeException {
    
    public OutboxEventCreationException(String message) {
        super(message);
    }
    
    public OutboxEventCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}