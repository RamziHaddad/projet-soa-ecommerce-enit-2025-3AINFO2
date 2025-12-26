package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.DeliveryCreationEvent;
import com.enit.orderservice.infrastructure.outbox.OutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing delivery creation events to Kafka.
 * Handles communication with the Delivery Service.
 * Uses Outbox Pattern for reliable event publishing.
 */
@ApplicationScoped
public class DeliveryEventProducer {

    private static final Logger LOG = Logger.getLogger(DeliveryEventProducer.class);
    private static final String TOPIC = "delivery-requests";

    @Inject
    OutboxService outboxService;

    /**
     * Publish delivery creation request event using Outbox Pattern
     * This is a fire-and-forget operation (async)
     * 
     * @param event The delivery creation request
     */
    public void publishRequest(DeliveryCreationEvent event) {
        LOG.infof("Saving delivery creation request to outbox for order: %s", event.getOrderId());
        
        try {
            outboxService.saveEvent(
                event.getOrderId(),
                "DeliveryCreationRequest",
                TOPIC,
                event
            );
            
            LOG.infof("Delivery creation request saved to outbox for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save delivery creation request to outbox for order: %s", event.getOrderId());
            throw new MessagePublishException(TOPIC, event, e);
        }
    }
}
