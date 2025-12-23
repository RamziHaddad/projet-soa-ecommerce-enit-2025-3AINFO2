package com.enit.orderservice.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health check for database connectivity
 * Verifies that the application can connect to and query the database
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        try {
            // Simple query to verify database connectivity
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            
            return HealthCheckResponse
                    .named("Database connection")
                    .up()
                    .withData("type", "H2")
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse
                    .named("Database connection")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
