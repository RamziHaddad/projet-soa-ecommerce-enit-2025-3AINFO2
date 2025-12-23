package com.enit.orderservice.infrastructure.exception;

/**
 * Thrown when database persistence operation fails
 */
public class PersistenceException extends RuntimeException {
    
    private final String operation;
    private final String entityType;
    
    public PersistenceException(String operation, String entityType, String message) {
        super(String.format("Persistence operation '%s' failed for %s: %s", 
            operation, entityType, message));
        this.operation = operation;
        this.entityType = entityType;
    }
    
    public PersistenceException(String operation, String entityType, Throwable cause) {
        super(String.format("Persistence operation '%s' failed for %s", operation, entityType), cause);
        this.operation = operation;
        this.entityType = entityType;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getEntityType() {
        return entityType;
    }
}
