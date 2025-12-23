package com.enit.orderservice.application.exception;

import java.util.UUID;

/**
 * Thrown when saga compensation (rollback) fails
 */
public class SagaCompensationFailedException extends RuntimeException {
    
    private final UUID orderId;
    private final String failedStep;
    
    public SagaCompensationFailedException(UUID orderId, String failedStep, String message) {
        super(String.format("Saga compensation failed for order %s at step '%s': %s", 
            orderId, failedStep, message));
        this.orderId = orderId;
        this.failedStep = failedStep;
    }
    
    public SagaCompensationFailedException(UUID orderId, String failedStep, Throwable cause) {
        super(String.format("Saga compensation failed for order %s at step '%s'", 
            orderId, failedStep), cause);
        this.orderId = orderId;
        this.failedStep = failedStep;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public String getFailedStep() {
        return failedStep;
    }
}
