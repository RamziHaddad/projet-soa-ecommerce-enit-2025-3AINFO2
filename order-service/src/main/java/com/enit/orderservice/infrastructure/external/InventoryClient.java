package com.enit.orderservice.infrastructure.external;

import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.infrastructure.resilience.InventoryServiceFallback;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;

/**
 * REST Client for Inventory Service with Circuit Breaker pattern
 * Prevents cascade failures and provides graceful degradation
 */
@RegisterRestClient(configKey = "inventory-service")
public interface InventoryClient {

    @POST
    @Path("/inventory/reserve")
    @Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS, jitter = 100)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
        requestVolumeThreshold = 4,     // Min requests before circuit can open
        failureRatio = 0.5,              // 50% failure rate opens circuit
        delay = 10000,                   // Wait 10s before half-open
        successThreshold = 2             // 2 successful calls to close circuit
    )
    @Fallback(InventoryServiceFallback.class)
    void reserveStock(Order order);

    @POST
    @Path("/inventory/release")
    @Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS, jitter = 100)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
        requestVolumeThreshold = 4,
        failureRatio = 0.5,
        delay = 10000,
        successThreshold = 2
    )
    @Fallback(InventoryServiceFallback.class)
    void releaseStock(Order order);
}
