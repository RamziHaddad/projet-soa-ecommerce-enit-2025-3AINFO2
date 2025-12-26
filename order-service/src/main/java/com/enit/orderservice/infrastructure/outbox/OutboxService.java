package com.enit.orderservice.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * Service for creating outbox events
 * Saves events to database instead of directly publishing to Kafka
 */
@ApplicationScoped
public class OutboxService {

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Save an event to the outbox
     * This is called instead of directly publishing to Kafka
     */
    @Transactional
    public OutboxEvent saveEvent(UUID orderId, String eventType, String topic, Object eventPayload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventPayload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(orderId)
                    .aggregateType("Order")
                    .eventType(eventType)
                    .topic(topic)
                    .payload(jsonPayload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxRepository.persist(outboxEvent);
            return outboxEvent;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }

    /**
     * Get outbox statistics
     */
    public OutboxStats getStats() {
        return OutboxStats.builder()
                .pendingCount(outboxRepository.countByStatus(OutboxStatus.PENDING))
                .publishedCount(outboxRepository.countByStatus(OutboxStatus.PUBLISHED))
                .failedCount(outboxRepository.countByStatus(OutboxStatus.FAILED))
                .build();
    }
}
