package com.enit.orderservice.application.exception;

import java.util.UUID;

/**
 * Thrown when saga execution exceeds timeout threshold
 */
public class SagaTimeoutException extends RuntimeException {
    
    private final UUID orderId;
    private final String currentStep;
    private final long timeoutMillis;
    
    public SagaTimeoutException(UUID orderId, String currentStep, long timeoutMillis) {
        super(String.format("Saga timeout for order %s at step '%s' after %d ms", 
            orderId, currentStep, timeoutMillis));
        this.orderId = orderId;
        this.currentStep = currentStep;
        this.timeoutMillis = timeoutMillis;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public long getTimeoutMillis() {
        return timeoutMillis;
    }
}
