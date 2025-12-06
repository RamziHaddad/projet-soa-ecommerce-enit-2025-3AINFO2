package ecommerce.pricing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.pricing.entity.OutboxEvent;
import ecommerce.pricing.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OutboxEvent createOutboxEvent(String aggregateType, String aggregateId,
                                         String eventType, Object eventPayload) {
        try {
            String payload = objectMapper.writeValueAsString(eventPayload);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType(aggregateType);
            outboxEvent.setAggregateId(aggregateId);
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayload(payload);
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEvent.setStatus(OutboxEvent.OutboxStatus.PENDING);
            outboxEvent.setRetryCount(0);

            return outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Error creating outbox event", e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }

    @Transactional
    public void markAsProcessed(Long eventId) {
        outboxRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxEvent.OutboxStatus.PROCESSED);
            event.setProcessedAt(LocalDateTime.now());
            outboxRepository.save(event);
        });
    }

    @Transactional
    public void markAsFailed(Long eventId, String errorMessage) {
        outboxRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxEvent.OutboxStatus.FAILED);
            event.setRetryCount(event.getRetryCount() + 1);
            event.setErrorMessage(errorMessage);
            outboxRepository.save(event);
        });
    }
}