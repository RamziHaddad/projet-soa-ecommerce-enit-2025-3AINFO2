package org.com.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.com.entities.OutboxEvent;
import org.com.entities.Product;
import org.com.exceptions.OutboxEventCreationException;
import org.com.repository.OutboxRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class OutboxService {

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    ObjectMapper objectMapper;

    public void createProductEvent(Product product, String eventType) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("Product");
            event.setAggregateId(product.getId());
            event.setEventType(eventType);
            event.setPayload(objectMapper.writeValueAsString(product));
            event.setStatus("PENDING");
            
            outboxRepository.insert(event);
        } catch (Exception e) {
            throw new OutboxEventCreationException("Failed to create outbox event", e);
        }
    }

    @Transactional
    public List<OutboxEvent> getPendingEvents(int limit) {
        return outboxRepository.findPendingEvents(limit);
    }
   @Transactional
    public List<OutboxEvent> getEventsByStatus(String status) {
        return outboxRepository.findByStatus(status);
    }
    @Transactional
    public List<OutboxEvent> getEventsByAggregateId(UUID aggregateId) {
        return outboxRepository.findByAggregateId(aggregateId);
    }
    @Transactional
    public Map<String, Object> getStats() {
        return outboxRepository.getStats();
    }
    @Transactional
    public void markAsProcessed(UUID eventId) {
        outboxRepository.markAsProcessed(eventId);
    }
   @Transactional
    public void markAsFailed(UUID eventId) {
        outboxRepository.markAsFailed(eventId);
    }
   @Transactional
    public void incrementRetryCount(UUID eventId) {
        outboxRepository.incrementRetryCount(eventId);
    }
   @Transactional
    public void resetForRetry(UUID eventId) {
        outboxRepository.resetForRetry(eventId);
    }
}