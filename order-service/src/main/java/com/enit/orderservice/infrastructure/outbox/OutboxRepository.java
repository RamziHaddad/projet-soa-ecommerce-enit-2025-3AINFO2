package com.enit.orderservice.infrastructure.outbox;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing Outbox events
 */
@ApplicationScoped
public class OutboxRepository implements PanacheRepository<OutboxEvent> {

    /**
     * Find pending events that need to be published
     */
    public List<OutboxEvent> findPendingEvents(int limit) {
        return find("status = ?1 ORDER BY createdAt ASC", OutboxStatus.PENDING)
                .page(0, limit)
                .list();
    }

    /**
     * Find failed events that can be retried
     */
    public List<OutboxEvent> findFailedEventsForRetry(int maxRetries, int limit) {
        return find("status = ?1 AND retryCount < ?2 ORDER BY createdAt ASC", 
                    OutboxStatus.FAILED, maxRetries)
                .page(0, limit)
                .list();
    }

    /**
     * Find events by aggregate (Order) ID
     */
    public List<OutboxEvent> findByAggregateId(UUID aggregateId) {
        return find("aggregateId", aggregateId).list();
    }

    /**
     * Find events by status
     */
    public List<OutboxEvent> findByStatus(OutboxStatus status) {
        return find("status", status).list();
    }

    /**
     * Delete old published events (cleanup)
     */
    public long deleteOldPublishedEvents(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return delete("status = ?1 AND publishedAt < ?2", OutboxStatus.PUBLISHED, cutoffDate);
    }

    /**
     * Count events by status
     */
    public long countByStatus(OutboxStatus status) {
        return count("status", status);
    }
}
