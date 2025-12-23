package com.enit.orderservice.infrastructure.messaging.consumer;

import com.enit.orderservice.application.saga.OrderSagaOrchestrator;
import com.enit.orderservice.infrastructure.exception.MessageConsumptionException;
import com.enit.orderservice.infrastructure.messaging.events.PricingResponseEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Consumer for pricing service responses
 * Listens to pricing-responses topic and forwards to saga orchestrator
 */
@ApplicationScoped
public class PricingEventConsumer {

    private static final Logger LOG = Logger.getLogger(PricingEventConsumer.class);

    @Inject
    OrderSagaOrchestrator sagaOrchestrator;

    /**
     * Consumes pricing response events from Kafka
     * @param event The pricing response containing total price calculation
     */
    @Incoming("pricing-responses")
    @Transactional
    public void onPricingResponse(PricingResponseEvent event) {
        LOG.infof("Received pricing response for order: %s, status: %s", 
                  event.getOrderId(), event.getStatus());
        
        try {
            sagaOrchestrator.handlePricingResponse(event);
        } catch (Exception e) {
            LOG.errorf(e, "Error processing pricing response for order: %s", event.getOrderId());
            throw new MessageConsumptionException("pricing-responses", "order-service-group", e);
        }
    }
}
