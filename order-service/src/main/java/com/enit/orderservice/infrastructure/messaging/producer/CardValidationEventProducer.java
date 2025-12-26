package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.CardValidationRequestEvent;
import com.enit.orderservice.infrastructure.outbox.OutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing card validation events to Kafka.
 * Handles communication with the Payment Service for card validation.
 * Uses Outbox Pattern for reliable event publishing.
 */
@ApplicationScoped
public class CardValidationEventProducer {

    private static final Logger LOG = Logger.getLogger(CardValidationEventProducer.class);
    private static final String TOPIC = "card-validation-requests";

    @Inject
    OutboxService outboxService;

    /**
     * Publish card validation request event using Outbox Pattern
     * 
     * @param event The card validation request containing card details
     */
    public void publishRequest(CardValidationRequestEvent event) {
        LOG.infof("Saving card validation request to outbox for order: %s", event.getOrderId());
        
        try {
            outboxService.saveEvent(
                event.getOrderId(),
                "CardValidationRequest",
                TOPIC,
                event
            );
            
            LOG.infof("Card validation request saved to outbox for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save card validation request to outbox for order: %s", event.getOrderId());
            throw new MessagePublishException(TOPIC, event, e);
        }
    }
}
