package com.enit.orderservice.infrastructure.outbox;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Scheduled job that publishes pending outbox events to Kafka
 * Runs every 5 seconds to ensure timely delivery
 */
@ApplicationScoped
public class OutboxPublisher {

    private static final Logger LOG = Logger.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRIES = 3;

    @Inject
    OutboxRepository outboxRepository;

    @Channel("pricing-requests")
    Emitter<String> pricingEmitter;

    @Channel("inventory-requests")
    Emitter<String> inventoryEmitter;

    @Channel("card-validation-requests")
    Emitter<String> cardValidationEmitter;

    @Channel("payment-requests")
    Emitter<String> paymentEmitter;

    @Channel("delivery-requests")
    Emitter<String> deliveryEmitter;

    @Channel("notifications")
    Emitter<String> notificationEmitter;

    /**
     * Scheduled job that runs every 5 seconds
     * Publishes pending events from outbox to Kafka
     */
    @Scheduled(every = "5s")
    @Transactional
    public void publishPendingEvents() {
        try {
            // Publish pending events
            List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(BATCH_SIZE);
            if (!pendingEvents.isEmpty()) {
                LOG.infof("Publishing %d pending outbox events", pendingEvents.size());
                pendingEvents.forEach(this::publishEvent);
            }

            // Retry failed events
            List<OutboxEvent> failedEvents = outboxRepository.findFailedEventsForRetry(MAX_RETRIES, BATCH_SIZE);
            if (!failedEvents.isEmpty()) {
                LOG.infof("Retrying %d failed outbox events", failedEvents.size());
                failedEvents.forEach(this::publishEvent);
            }

        } catch (Exception e) {
            LOG.error("Error publishing outbox events", e);
        }
    }

    /**
     * Publish a single outbox event to the appropriate Kafka topic
     */
    private void publishEvent(OutboxEvent event) {
        try {
            Emitter<String> emitter = getEmitterForTopic(event.getTopic());
            
            if (emitter == null) {
                LOG.errorf("No emitter found for topic: %s", event.getTopic());
                event.markAsFailed("No emitter configured for topic: " + event.getTopic());
                outboxRepository.persist(event);
                return;
            }

            // Create Kafka message with metadata
            OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String>builder()
                    .withKey(event.getAggregateId().toString())
                    .build();

            Message<String> message = Message.of(event.getPayload())
                    .addMetadata(metadata);

            // Send to Kafka
            emitter.send(message);

            // Mark as published
            event.markAsPublished();
            outboxRepository.persist(event);

            LOG.debugf("Published outbox event %s to topic %s", event.getId(), event.getTopic());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish outbox event %s", event.getId());
            event.markAsFailed(e.getMessage());
            outboxRepository.persist(event);
        }
    }

    /**
     * Get the appropriate Kafka emitter based on topic name
     */
    private Emitter<String> getEmitterForTopic(String topic) {
        return switch (topic) {
            case "pricing-requests" -> pricingEmitter;
            case "inventory-requests" -> inventoryEmitter;
            case "card-validation-requests" -> cardValidationEmitter;
            case "payment-requests" -> paymentEmitter;
            case "delivery-requests" -> deliveryEmitter;
            case "notifications" -> notificationEmitter;
            default -> null;
        };
    }

    /**
     * Cleanup old published events (runs daily)
     * Keeps database size manageable
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    @Transactional
    public void cleanupOldEvents() {
        try {
            long deleted = outboxRepository.deleteOldPublishedEvents(7); // Delete events older than 7 days
            if (deleted > 0) {
                LOG.infof("Cleaned up %d old outbox events", deleted);
            }
        } catch (Exception e) {
            LOG.error("Error cleaning up old outbox events", e);
        }
    }
}
