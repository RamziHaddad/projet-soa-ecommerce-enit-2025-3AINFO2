package com.enit.orderservice.infrastructure.resilience;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Circuit Breaker status information for monitoring
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerStatus {
    private String serviceName;
    private String state;           // CLOSED, OPEN, HALF_OPEN
    private long successfulCalls;
    private long failedCalls;
    private double failureRate;
    private String description;
}
