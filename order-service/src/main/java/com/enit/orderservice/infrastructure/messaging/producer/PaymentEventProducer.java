package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.PaymentRequestEvent;
import com.enit.orderservice.infrastructure.outbox.OutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing payment-related events to Kafka.
 * Handles communication with the Payment Service.
 * Uses Outbox Pattern for reliable event publishing.
 */
@ApplicationScoped
public class PaymentEventProducer {

    private static final Logger LOG = Logger.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payment-requests";

    @Inject
    OutboxService outboxService;

    /**
     * Publish payment request event using Outbox Pattern (for both payment and refund operations)
     * 
     * @param event The payment request containing payment or refund details
     */
    public void publishRequest(PaymentRequestEvent event) {
        String operation = event.isRefund() ? "refund" : "payment";
        LOG.infof("Saving %s request to outbox for order: %s", operation, event.getOrderId());
        
        try {
            outboxService.saveEvent(
                event.getOrderId(),
                "PaymentRequest",
                TOPIC,
                event
            );
            
            LOG.infof("Payment request saved to outbox for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save %s request to outbox for order: %s", operation, event.getOrderId());
            throw new MessagePublishException(TOPIC, event, e);
        }
    }
}
