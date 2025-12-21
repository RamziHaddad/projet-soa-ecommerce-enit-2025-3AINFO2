package org.com.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.com.entities.OutboxEvent;

@ApplicationScoped
public class OutboxRepository {

    @Inject
    EntityManager em;

   
    public OutboxEvent insert(OutboxEvent event) {
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        em.persist(event);
        return event;
    }

    public List<OutboxEvent> findPendingEvents(int limit) {
        return em.createQuery(
            "SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' AND o.retryCount < 3 ORDER BY o.createdAt ASC", 
            OutboxEvent.class)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setMaxResults(limit)
            .getResultList();
    }

    public List<OutboxEvent> findByStatus(String status) {
        return em.createQuery(
            "SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt DESC", 
            OutboxEvent.class)
            .setParameter("status", status)
            .setMaxResults(50)
            .getResultList();
    }

    public List<OutboxEvent> findByAggregateId(UUID aggregateId) {
        return em.createQuery(
            "SELECT o FROM OutboxEvent o WHERE o.aggregateId = :aggregateId ORDER BY o.createdAt DESC", 
            OutboxEvent.class)
            .setParameter("aggregateId", aggregateId)
            .getResultList();
    }

    public Map<String, Object> getStats() {
        Long pending = em.createQuery("SELECT COUNT(o) FROM OutboxEvent o WHERE o.status = 'PENDING'", Long.class).getSingleResult();
        Long processed = em.createQuery("SELECT COUNT(o) FROM OutboxEvent o WHERE o.status = 'PROCESSED'", Long.class).getSingleResult();
        Long failed = em.createQuery("SELECT COUNT(o) FROM OutboxEvent o WHERE o.status = 'FAILED'", Long.class).getSingleResult();
        
        return Map.of(
            "pending", pending,
            "processed", processed,
            "failed", failed,
            "total", pending + processed + failed
        );
    }

    public void markAsProcessed(UUID eventId) {
        OutboxEvent event = em.find(OutboxEvent.class, eventId);
        if (event != null) {
            event.setStatus("PROCESSED");
            event.setProcessedAt(LocalDateTime.now());
        }
    }

    public void markAsFailed(UUID eventId) {
        OutboxEvent event = em.find(OutboxEvent.class, eventId);
        if (event != null) {
            event.setStatus("FAILED");
            event.setProcessedAt(LocalDateTime.now());
        }
    }

    public void incrementRetryCount(UUID eventId) {
        OutboxEvent event = em.find(OutboxEvent.class, eventId);
        if (event != null) {
            event.setRetryCount(event.getRetryCount() + 1);
        }
    }

    public void resetForRetry(UUID eventId) {
        OutboxEvent event = em.find(OutboxEvent.class, eventId);
        if (event != null && "FAILED".equals(event.getStatus())) {
            event.setStatus("PENDING");
            event.setRetryCount(0);
            event.setProcessedAt(null);
        }
    }
}
