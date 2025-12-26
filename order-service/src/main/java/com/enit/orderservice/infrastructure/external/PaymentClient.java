package com.enit.orderservice.infrastructure.external;


import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.infrastructure.resilience.PaymentServiceFallback;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;

/**
 * REST Client for Payment Service with Circuit Breaker pattern
 * Prevents cascade failures and provides graceful degradation
 */
@RegisterRestClient(configKey = "payment-service")
public interface PaymentClient {

    @POST
    @Path("/payment/process")
    @Retry(maxRetries = 2, delay = 300, delayUnit = ChronoUnit.MILLIS, jitter = 100)
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
        requestVolumeThreshold = 4,     // Min requests before circuit can open
        failureRatio = 0.5,              // 50% failure rate opens circuit
        delay = 15000,                   // Wait 15s before half-open (longer for payment)
        successThreshold = 3             // 3 successful calls to close circuit
    )
    @Fallback(PaymentServiceFallback.class)
    void processPayment(Order order);
}
