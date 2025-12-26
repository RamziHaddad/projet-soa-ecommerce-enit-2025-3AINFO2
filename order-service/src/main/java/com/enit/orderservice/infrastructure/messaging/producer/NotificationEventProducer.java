package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.messaging.events.NotificationEvent;
import com.enit.orderservice.infrastructure.outbox.OutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing notification events to Kafka.
 * Handles communication with the Notification Service.
 * Uses Outbox Pattern for reliable event publishing.
 */
@ApplicationScoped
public class NotificationEventProducer {

    private static final Logger LOG = Logger.getLogger(NotificationEventProducer.class);
    private static final String TOPIC = "notifications";

    @Inject
    OutboxService outboxService;

    /**
     * Publish notification event using Outbox Pattern
     * This is a fire-and-forget operation (async)
     * 
     * @param event The notification event
     */
    public void publishNotification(NotificationEvent event) {
        LOG.infof("Saving notification to outbox for order: %s, type: %s", 
                  event.getOrderId(), event.getType());
        
        try {
            outboxService.saveEvent(
                event.getOrderId(),
                "Notification",
                TOPIC,
                event
            );
            
            LOG.infof("Notification saved to outbox for order: %s", event.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save notification to outbox for order: %s", event.getOrderId());
            throw new MessagePublishException(TOPIC, event, e);
        }
    }
}
