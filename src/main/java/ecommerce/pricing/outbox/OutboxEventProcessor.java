package ecommerce.pricing.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.pricing.entity.OutboxEvent;
import ecommerce.pricing.kafka.event.PriceChangeKafkaEvent;
import ecommerce.pricing.kafka.event.PromotionExpiringKafkaEvent;
import ecommerce.pricing.repository.OutboxRepository;
import ecommerce.pricing.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxRepository outboxRepository;
    private final OutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.price-change}")
    private String priceChangeTopic;

    @Value("${kafka.topics.promotion-expiring}")
    private String promotionExpiringTopic;

    @Scheduled(fixedDelay = 5000) // Toutes les 5 secondes
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents();

        log.info("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                sendToKafka(event);
                outboxService.markAsProcessed(event.getId());
                log.info("Successfully processed outbox event {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event {}", event.getId(), e);
                outboxService.markAsFailed(event.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 30000) // Toutes les 30 secondes pour les retry
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxRepository.findFailedEventsForRetry();

        if (!failedEvents.isEmpty()) {
            log.info("Retrying {} failed outbox events", failedEvents.size());

            for (OutboxEvent event : failedEvents) {
                try {
                    sendToKafka(event);
                    outboxService.markAsProcessed(event.getId());
                    log.info("Successfully retried outbox event {}", event.getId());
                } catch (Exception e) {
                    log.error("Retry failed for outbox event {}", event.getId(), e);
                    outboxService.markAsFailed(event.getId(), e.getMessage());
                }
            }
        }
    }

    private void sendToKafka(OutboxEvent event) throws Exception {
        String topic;
        Object kafkaEvent;

        switch (event.getEventType()) {
            case "PRICE_CHANGED":
                topic = priceChangeTopic;
                kafkaEvent = objectMapper.readValue(event.getPayload(), PriceChangeKafkaEvent.class);
                break;
            case "PROMOTION_EXPIRING":
                topic = promotionExpiringTopic;
                kafkaEvent = objectMapper.readValue(event.getPayload(), PromotionExpiringKafkaEvent.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }

        kafkaTemplate.send(topic, event.getAggregateId(), kafkaEvent).get();
    }
}