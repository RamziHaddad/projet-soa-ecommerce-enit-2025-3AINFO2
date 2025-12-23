package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.PricingRequestEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing pricing-related events to Kafka.
 * Handles communication with the Pricing Service.
 */
@ApplicationScoped
public class PricingEventProducer {

    private static final Logger LOG = Logger.getLogger(PricingEventProducer.class);

    @Inject
    @Channel("pricing-requests")
    Emitter<PricingRequestEvent> pricingEmitter;

    /**
     * Publish pricing request event to Kafka
     * 
     * @param event The pricing request containing order items to be priced
     */
    public void publishRequest(PricingRequestEvent event) {
        LOG.infof("Publishing pricing request for order: %s", event.getOrderId());
        
        try {
            // Create message with Kafka metadata (key = orderId for partitioning)
            Message<PricingRequestEvent> message = Message.of(event)
                    .addMetadata(OutgoingKafkaRecordMetadata.builder()
                            .withKey(event.getOrderId().toString())
                            .build());
            
            // Send to Kafka topic: pricing-requests
            pricingEmitter.send(message);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish pricing request for order: %s", event.getOrderId());
            throw new MessagePublishException("pricing-requests", event, e);
        }
        
        LOG.infof("Pricing request published successfully for order: %s", event.getOrderId());
    }
}
