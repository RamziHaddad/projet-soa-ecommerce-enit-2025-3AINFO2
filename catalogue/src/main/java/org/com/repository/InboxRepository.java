package org.com.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.com.entities.InboxEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class InboxRepository {

    @Inject
    EntityManager em;

    public boolean alreadyProcessed(UUID eventId) {
        Long count = em.createQuery(
            "SELECT COUNT(i) FROM InboxEvent i WHERE i.eventId = :eventId", 
            Long.class)
            .setParameter("eventId", eventId)
            .getSingleResult();
        return count > 0;
    }

    @Transactional
    public InboxEvent insert(InboxEvent event) {
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        em.persist(event);
        return event;
    }

    public List<InboxEvent> findAll(int limit) {
        return em.createQuery(
            "SELECT i FROM InboxEvent i ORDER BY i.processedAt DESC", 
            InboxEvent.class)
            .setMaxResults(limit)
            .getResultList();
    }

    public List<InboxEvent> findByAggregateId(String aggregateId) {
        return em.createQuery(
            "SELECT i FROM InboxEvent i WHERE i.aggregateId = :aggregateId ORDER BY i.processedAt DESC", 
            InboxEvent.class)
            .setParameter("aggregateId", aggregateId)
            .getResultList();
    }

    public Map<String, Object> getStats() {
        Long total = em.createQuery(
            "SELECT COUNT(i) FROM InboxEvent i", 
            Long.class)
            .getSingleResult();
        
        Long lastHour = em.createQuery(
            "SELECT COUNT(i) FROM InboxEvent i WHERE i.processedAt >= :since", 
            Long.class)
            .setParameter("since", java.time.LocalDateTime.now().minusHours(1))
            .getSingleResult();
        
        return Map.of(
            "total", total,
            "lastHour", lastHour
        );
    }
}
