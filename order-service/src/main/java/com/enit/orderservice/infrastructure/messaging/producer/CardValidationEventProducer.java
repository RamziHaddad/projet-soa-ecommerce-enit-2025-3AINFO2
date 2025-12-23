package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.CardValidationRequestEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing card validation events to Kafka.
 * Handles communication with the Payment Service for card validation.
 */
@ApplicationScoped
public class CardValidationEventProducer {

    private static final Logger LOG = Logger.getLogger(CardValidationEventProducer.class);

    @Inject
    @Channel("card-validation-requests")
    Emitter<CardValidationRequestEvent> cardValidationEmitter;

    /**
     * Publish card validation request event to Kafka
     * 
     * @param event The card validation request containing card details
     */
    public void publishRequest(CardValidationRequestEvent event) {
        LOG.infof("Publishing card validation request for order: %s", event.getOrderId());
        
        try {
            // Create message with Kafka metadata (key = orderId for partitioning)
            Message<CardValidationRequestEvent> message = Message.of(event)
                    .addMetadata(OutgoingKafkaRecordMetadata.builder()
                            .withKey(event.getOrderId().toString())
                            .build());
            
            // Send to Kafka topic: card-validation-requests
            cardValidationEmitter.send(message);
            
            LOG.infof("Card validation request published successfully for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish card validation request for order: %s", event.getOrderId());
            throw new MessagePublishException("card-validation-requests", event, e);
        }
    }
}
