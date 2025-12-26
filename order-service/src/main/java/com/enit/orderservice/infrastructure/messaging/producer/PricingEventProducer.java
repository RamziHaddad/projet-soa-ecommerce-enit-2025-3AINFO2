package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.PricingRequestEvent;
import com.enit.orderservice.infrastructure.outbox.OutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing pricing-related events to Kafka.
 * Handles communication with the Pricing Service.
 * Uses Outbox Pattern for reliable event publishing.
 */
@ApplicationScoped
public class PricingEventProducer {

    private static final Logger LOG = Logger.getLogger(PricingEventProducer.class);
    private static final String TOPIC = "pricing-requests";

    @Inject
    OutboxService outboxService;

    /**
     * Publish pricing request event using Outbox Pattern
     * Event is first saved to database, then published asynchronously
     * 
     * @param event The pricing request containing order items to be priced
     */
    public void publishRequest(PricingRequestEvent event) {
        LOG.infof("Saving pricing request to outbox for order: %s", event.getOrderId());
        
        try {
            // Save to outbox table instead of direct Kafka publish
            outboxService.saveEvent(
                event.getOrderId(),
                "PricingRequest",
                TOPIC,
                event
            );
            
            LOG.infof("Pricing request saved to outbox for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save pricing request to outbox for order: %s", event.getOrderId());
            throw new MessagePublishException(TOPIC, event, e);
        }
    }
}
