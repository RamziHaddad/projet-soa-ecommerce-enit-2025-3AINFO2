package com.enit.orderservice.domaine.exception;

import com.enit.orderservice.domaine.model.OrderStatus;

import java.util.UUID;

/**
 * Thrown when trying to perform an operation on an order in an invalid state
 */
public class InvalidOrderStateException extends RuntimeException {
    
    private final UUID orderId;
    private final OrderStatus currentStatus;
    private final String attemptedOperation;
    
    public InvalidOrderStateException(UUID orderId, OrderStatus currentStatus, String attemptedOperation) {
        super(String.format("Cannot %s order %s in status %s", 
            attemptedOperation, orderId, currentStatus));
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedOperation = attemptedOperation;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public String getAttemptedOperation() {
        return attemptedOperation;
    }
}
