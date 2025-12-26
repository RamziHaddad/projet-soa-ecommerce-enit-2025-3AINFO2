package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.InventoryRequestEvent;
import com.enit.orderservice.infrastructure.outbox.OutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing inventory-related events to Kafka.
 * Handles communication with the Inventory Service.
 * Supports both reserve (forward) and release (compensation) operations.
 * Uses Outbox Pattern for reliable event publishing.
 */
@ApplicationScoped
public class InventoryEventProducer {

    private static final Logger LOG = Logger.getLogger(InventoryEventProducer.class);
    private static final String TOPIC = "inventory-requests";

    @Inject
    OutboxService outboxService;

    /**
     * Publish inventory request event using Outbox Pattern.
     * Handles both reserve (release=false) and release (release=true) operations.
     * 
     * @param event The inventory request containing items to reserve or release
     */
    public void publishRequest(InventoryRequestEvent event) {
        String operation = event.isRelease() ? "release" : "reserve";
        LOG.infof("Saving inventory %s request to outbox for order: %s", operation, event.getOrderId());
        
        try {
            // Save to outbox table instead of direct Kafka publish
            outboxService.saveEvent(
                event.getOrderId(),
                "InventoryRequest",
                TOPIC,
                event
            );
            
            LOG.infof("Inventory %s request saved to outbox for order: %s", operation, event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save inventory %s request to outbox for order: %s", operation, event.getOrderId());
            throw new MessagePublishException(TOPIC, event, e);
        }
    }
}
