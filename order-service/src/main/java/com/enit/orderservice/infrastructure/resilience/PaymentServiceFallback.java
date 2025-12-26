package com.enit.orderservice.infrastructure.resilience;

import com.enit.orderservice.domaine.model.Order;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.jboss.logging.Logger;

/**
 * Fallback handler for Payment Service when circuit is open or service fails
 * Provides graceful degradation instead of complete failure
 */
@ApplicationScoped
public class PaymentServiceFallback implements FallbackHandler<Void> {

    private static final Logger LOG = Logger.getLogger(PaymentServiceFallback.class);

    @Override
    public Void handle(ExecutionContext context) {
        Order order = (Order) context.getParameters()[0];
        
        LOG.errorf("Payment service fallback triggered for order: %s", order.getOrderId());
        
        // In a real system, you might:
        // 1. Queue payment for manual processing
        // 2. Try alternative payment gateway
        // 3. Send email to admin
        // 4. Trigger saga compensation
        
        throw new RuntimeException(
            "Payment service unavailable. Circuit breaker activated."
        );
    }
}
