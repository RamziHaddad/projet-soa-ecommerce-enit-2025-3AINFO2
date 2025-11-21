package org.com.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.com.entities.OutboxEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MessageBrokerService {

    private static final Logger LOG = Logger.getLogger(MessageBrokerService.class);
    private static final int TIMEOUT_SECONDS = 10;

    @Inject
    @Channel("product-events")
    Emitter<String> productEventsEmitter;

    public void sendToKafka(OutboxEvent event) {
        String message = String.format(
            "{\"eventType\":\"%s\",\"aggregateId\":\"%s\",\"payload\":%s}",
            event.getEventType(),
            event.getAggregateId(),
            event.getPayload()
        );
        
        try {
            LOG.infof("Sending event %s to Kafka...", event.getId());
            
            // Envoie et ATTEND la confirmation (bloquant)
            CompletionStage<Void> result = productEventsEmitter.send(message);
            
            // Attend jusqu'à TIMEOUT_SECONDS pour la confirmation
            result.toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            LOG.infof("Event %s sent to Kafka successfully", event.getId());
            
        } catch (Exception e) {
            LOG.errorf("Failed to send event %s to Kafka: %s", event.getId(), e.getMessage());
            throw new RuntimeException("Kafka send failed: " + e.getMessage(), e);
        }
    }
}