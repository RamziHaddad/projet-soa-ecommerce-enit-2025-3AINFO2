package com.enit.orderservice.infrastructure.messaging.consumer;

import com.enit.orderservice.application.saga.OrderSagaOrchestrator;
import com.enit.orderservice.infrastructure.exception.MessageConsumptionException;
import com.enit.orderservice.infrastructure.messaging.events.InventoryResponseEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Consumer for inventory service responses
 * Listens to inventory-responses topic and forwards to saga orchestrator
 */
@ApplicationScoped
public class InventoryEventConsumer {

    private static final Logger LOG = Logger.getLogger(InventoryEventConsumer.class);

    @Inject
    OrderSagaOrchestrator sagaOrchestrator;

    /**
     * Consumes inventory response events from Kafka
     * @param event The inventory response containing reservation status
     */
    @Incoming("inventory-responses")
    @Transactional
    public void onInventoryResponse(InventoryResponseEvent event) {
        LOG.infof("Received inventory response for order: %s, reserved: %s", 
                  event.getOrderId(), event.isReserved());
        
        try {
            sagaOrchestrator.handleInventoryResponse(event);
        } catch (Exception e) {
            LOG.errorf(e, "Error processing inventory response for order: %s", event.getOrderId());
            throw new MessageConsumptionException("inventory-responses", "order-service-group", e);
        }
    }
}
