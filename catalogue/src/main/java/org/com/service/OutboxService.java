package org.com.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.com.entities.OutboxEvent;
import org.com.entities.Product;
import org.com.exceptions.OutboxEventCreationException;
import org.com.repository.OutboxRepository;
import org.com.service.MessageBrokerService; 

@ApplicationScoped
public class OutboxService {

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    ObjectMapper objectMapper;

    @Transactional
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
}