package com.enit.orderservice.infrastructure.resilience;

import com.enit.orderservice.domaine.model.Order;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.jboss.logging.Logger;

/**
 * Fallback handler for Inventory Service when circuit is open or service fails
 * Provides graceful degradation instead of complete failure
 */
@ApplicationScoped
public class InventoryServiceFallback implements FallbackHandler<Void> {

    private static final Logger LOG = Logger.getLogger(InventoryServiceFallback.class);

    @Override
    public Void handle(ExecutionContext context) {
        Order order = (Order) context.getParameters()[0];
        String operation = context.getMethod().getName();
        
        LOG.errorf("Inventory service fallback triggered for %s operation, order: %s", 
                   operation, order.getOrderId());
        
        // In a real system, you might:
        // 1. Queue the request for later processing
        // 2. Send to alternative inventory system
        // 3. Use cached inventory data
        // 4. Throw a business exception to trigger saga compensation
        
        throw new RuntimeException(
            String.format("Inventory service unavailable for %s. Circuit breaker activated.", operation)
        );
    }
}
