package com.enit.orderservice.interfaces.restcontrollers;

import com.enit.orderservice.infrastructure.resilience.CircuitBreakerStatus;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * REST endpoint for monitoring circuit breaker status
 * Shows the state of circuit breakers for external services
 */
@Path("/circuit-breakers")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Circuit Breaker Monitoring", description = "Monitor circuit breaker states for external service calls")
public class CircuitBreakerController {

    @GET
    @Path("/status")
    @Operation(
        summary = "Get circuit breaker status",
        description = "Returns the state of all circuit breakers protecting external service calls. " +
                     "CLOSED = Normal operation, OPEN = Service unavailable, HALF_OPEN = Testing recovery"
    )
    @APIResponse(
        responseCode = "200",
        description = "Circuit breaker status retrieved successfully"
    )
    public List<CircuitBreakerStatus> getStatus() {
        // Note: In production, you would fetch actual circuit breaker metrics
        // from MicroProfile Metrics or SmallRye Fault Tolerance metrics
        // This is a simplified example showing the API structure
        
        List<CircuitBreakerStatus> statuses = new ArrayList<>();
        
        statuses.add(CircuitBreakerStatus.builder()
                .serviceName("Inventory Service")
                .state("CLOSED")
                .description("Circuit breaker is closed. Service is healthy and accepting requests.")
                .build());
        
        statuses.add(CircuitBreakerStatus.builder()
                .serviceName("Payment Service")
                .state("CLOSED")
                .description("Circuit breaker is closed. Service is healthy and accepting requests.")
                .build());
        
        return statuses;
    }
    
    @GET
    @Path("/info")
    @Operation(
        summary = "Get circuit breaker configuration",
        description = "Returns configuration details for all circuit breakers"
    )
    @APIResponse(
        responseCode = "200",
        description = "Circuit breaker configuration retrieved"
    )
    public CircuitBreakerInfo getInfo() {
        return CircuitBreakerInfo.builder()
                .requestVolumeThreshold(4)
                .failureRatio(0.5)
                .delayInMillis(10000)
                .successThreshold(2)
                .retryMaxAttempts(3)
                .retryDelayInMillis(200)
                .timeoutInSeconds(5)
                .description("Circuit breaker opens after 50% failure rate on 4+ requests. " +
                           "Waits 10s before testing recovery. Retries failed calls up to 3 times.")
                .build();
    }
    
    /**
     * Circuit Breaker configuration info
     */
    public static record CircuitBreakerInfo(
        int requestVolumeThreshold,
        double failureRatio,
        long delayInMillis,
        int successThreshold,
        int retryMaxAttempts,
        long retryDelayInMillis,
        int timeoutInSeconds,
        String description
    ) {
        public static CircuitBreakerInfoBuilder builder() {
            return new CircuitBreakerInfoBuilder();
        }
        
        public static class CircuitBreakerInfoBuilder {
            private int requestVolumeThreshold;
            private double failureRatio;
            private long delayInMillis;
            private int successThreshold;
            private int retryMaxAttempts;
            private long retryDelayInMillis;
            private int timeoutInSeconds;
            private String description;
            
            public CircuitBreakerInfoBuilder requestVolumeThreshold(int requestVolumeThreshold) {
                this.requestVolumeThreshold = requestVolumeThreshold;
                return this;
            }
            
            public CircuitBreakerInfoBuilder failureRatio(double failureRatio) {
                this.failureRatio = failureRatio;
                return this;
            }
            
            public CircuitBreakerInfoBuilder delayInMillis(long delayInMillis) {
                this.delayInMillis = delayInMillis;
                return this;
            }
            
            public CircuitBreakerInfoBuilder successThreshold(int successThreshold) {
                this.successThreshold = successThreshold;
                return this;
            }
            
            public CircuitBreakerInfoBuilder retryMaxAttempts(int retryMaxAttempts) {
                this.retryMaxAttempts = retryMaxAttempts;
                return this;
            }
            
            public CircuitBreakerInfoBuilder retryDelayInMillis(long retryDelayInMillis) {
                this.retryDelayInMillis = retryDelayInMillis;
                return this;
            }
            
            public CircuitBreakerInfoBuilder timeoutInSeconds(int timeoutInSeconds) {
                this.timeoutInSeconds = timeoutInSeconds;
                return this;
            }
            
            public CircuitBreakerInfoBuilder description(String description) {
                this.description = description;
                return this;
            }
            
            public CircuitBreakerInfo build() {
                return new CircuitBreakerInfo(
                    requestVolumeThreshold,
                    failureRatio,
                    delayInMillis,
                    successThreshold,
                    retryMaxAttempts,
                    retryDelayInMillis,
                    timeoutInSeconds,
                    description
                );
            }
        }
    }
}
