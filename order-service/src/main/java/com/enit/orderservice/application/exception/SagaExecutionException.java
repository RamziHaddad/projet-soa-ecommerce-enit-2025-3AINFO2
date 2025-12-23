package com.enit.orderservice.application.exception;

import java.util.UUID;

/**
 * Thrown when saga execution fails during normal flow (not compensation)
 */
public class SagaExecutionException extends RuntimeException {
    
    private final UUID orderId;
    private final String failedStep;
    
    public SagaExecutionException(UUID orderId, String failedStep, String message) {
        super(String.format("Saga execution failed for order %s at step '%s': %s", 
            orderId, failedStep, message));
        this.orderId = orderId;
        this.failedStep = failedStep;
    }
    
    public SagaExecutionException(UUID orderId, String failedStep, Throwable cause) {
        super(String.format("Saga execution failed for order %s at step '%s'", 
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
