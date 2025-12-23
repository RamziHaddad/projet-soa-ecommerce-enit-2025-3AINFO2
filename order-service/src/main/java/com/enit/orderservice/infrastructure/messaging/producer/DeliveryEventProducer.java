package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.DeliveryCreationEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing delivery creation events to Kafka.
 * Handles communication with the Delivery Service.
 */
@ApplicationScoped
public class DeliveryEventProducer {

    private static final Logger LOG = Logger.getLogger(DeliveryEventProducer.class);

    @Inject
    @Channel("delivery-requests")
    Emitter<DeliveryCreationEvent> deliveryEmitter;

    /**
     * Publish delivery creation request event to Kafka
     * This is a fire-and-forget operation (async)
     * 
     * @param event The delivery creation request
     */
    public void publishRequest(DeliveryCreationEvent event) {
        LOG.infof("Publishing delivery creation request for order: %s", event.getOrderId());
        
        try {
            // Create message with Kafka metadata (key = orderId for partitioning)
            Message<DeliveryCreationEvent> message = Message.of(event)
                    .addMetadata(OutgoingKafkaRecordMetadata.builder()
                            .withKey(event.getOrderId().toString())
                            .build());
            
            // Send to Kafka topic: delivery-requests
            deliveryEmitter.send(message);
            
            LOG.infof("Delivery creation request published successfully for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish delivery creation request for order: %s", event.getOrderId());
            throw new MessagePublishException("delivery-requests", event, e);
        }
    }
}
