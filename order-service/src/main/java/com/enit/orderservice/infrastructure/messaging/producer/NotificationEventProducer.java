package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.messaging.events.NotificationEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing notification events to Kafka.
 * Handles communication with the Notification Service.
 */
@ApplicationScoped
public class NotificationEventProducer {

    private static final Logger LOG = Logger.getLogger(NotificationEventProducer.class);

    @Inject
    @Channel("notifications")
    Emitter<NotificationEvent> notificationEmitter;

    /**
     * Publish notification event to Kafka
     * This is a fire-and-forget operation (async)
     * 
     * @param event The notification event
     */
    public void publishNotification(NotificationEvent event) {
        LOG.infof("Publishing notification for order: %s, type: %s", 
                  event.getOrderId(), event.getType());
        
        // Create message with Kafka metadata (key = customerId for partitioning)
        Message<NotificationEvent> message = Message.of(event)
                .addMetadata(OutgoingKafkaRecordMetadata.builder()
                        .withKey(event.getCustomerId())
                        .build());
        
        // Send to Kafka topic: notifications
        notificationEmitter.send(message);
        
        LOG.infof("Notification published successfully for order: %s", event.getOrderId());
    }
}
