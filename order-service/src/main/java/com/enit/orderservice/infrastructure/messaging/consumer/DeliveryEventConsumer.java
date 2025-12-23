package com.enit.orderservice.infrastructure.messaging.consumer;

import com.enit.orderservice.application.saga.OrderSagaOrchestrator;
import com.enit.orderservice.infrastructure.exception.MessageConsumptionException;
import com.enit.orderservice.infrastructure.messaging.events.DeliveryResponseEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Consumer for delivery service responses (optional)
 * Listens to delivery-responses topic for tracking updates
 */
@ApplicationScoped
public class DeliveryEventConsumer {

    private static final Logger LOG = Logger.getLogger(DeliveryEventConsumer.class);

    @Inject
    OrderSagaOrchestrator sagaOrchestrator;

    /**
     * Consumes delivery response events from Kafka
     * @param event The delivery response containing tracking information
     */
    @Incoming("delivery-responses")
    @Transactional
    public void onDeliveryResponse(DeliveryResponseEvent event) {
        LOG.infof("Received delivery response for order: %s, tracking: %s", 
                  event.getOrderId(), event.getTrackingNumber());
        
        try {
            sagaOrchestrator.handleDeliveryResponse(event);
        } catch (Exception e) {
            LOG.errorf(e, "Error processing delivery response for order: %s", event.getOrderId());
            throw new MessageConsumptionException("delivery-responses", "order-service-group", e);
        }
    }
}
