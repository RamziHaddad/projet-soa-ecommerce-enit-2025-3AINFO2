package com.enit.orderservice.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Basic liveness check
 * Indicates that the application is running and not stuck
 */
@Liveness
@ApplicationScoped
public class LivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse
                .named("Order Service")
                .up()
                .withData("service", "order-service")
                .withData("version", "1.0.0")
                .build();
    }
}
