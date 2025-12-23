package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.PaymentRequestEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing payment-related events to Kafka.
 * Handles communication with the Payment Service.
 */
@ApplicationScoped
public class PaymentEventProducer {

    private static final Logger LOG = Logger.getLogger(PaymentEventProducer.class);

    @Inject
    @Channel("payment-requests")
    Emitter<PaymentRequestEvent> paymentEmitter;

    /**
     * Publish payment request event to Kafka (for both payment and refund operations)
     * 
     * @param event The payment request containing payment or refund details
     */
    public void publishRequest(PaymentRequestEvent event) {
        String operation = event.isRefund() ? "refund" : "payment";
        LOG.infof("Publishing %s request for order: %s", operation, event.getOrderId());
        
        try {
            // Create message with Kafka metadata (key = orderId for partitioning)
            Message<PaymentRequestEvent> message = Message.of(event)
                    .addMetadata(OutgoingKafkaRecordMetadata.builder()
                            .withKey(event.getOrderId().toString())
                            .build());
            
            // Send to Kafka topic: payment-requests
            paymentEmitter.send(message);
            
            LOG.infof("Payment request published successfully for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish %s request for order: %s", operation, event.getOrderId());
            throw new MessagePublishException("payment-requests", event, e);
        }
    }
}
