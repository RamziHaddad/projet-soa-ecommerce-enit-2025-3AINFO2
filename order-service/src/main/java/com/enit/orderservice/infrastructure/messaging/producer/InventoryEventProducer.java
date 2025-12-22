package com.enit.orderservice.infrastructure.messaging.producer;

import com.enit.orderservice.infrastructure.messaging.events.InventoryRequestEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Producer responsible for publishing inventory-related events to Kafka.
 * Handles communication with the Inventory Service.
 * Supports both reserve (forward) and release (compensation) operations.
 */
@ApplicationScoped
public class InventoryEventProducer {

    private static final Logger LOG = Logger.getLogger(InventoryEventProducer.class);

    @Inject
    @Channel("inventory-requests")
    Emitter<InventoryRequestEvent> inventoryEmitter;

    /**
     * Publish inventory request event to Kafka.
     * Handles both reserve (release=false) and release (release=true) operations.
     * 
     * @param event The inventory request containing items to reserve or release
     */
    public void publishRequest(InventoryRequestEvent event) {
        String operation = event.isRelease() ? "release" : "reserve";
        LOG.infof("Publishing inventory %s request for order: %s", operation, event.getOrderId());
        
        // Create message with Kafka metadata (key = orderId for partitioning)
        Message<InventoryRequestEvent> message = Message.of(event)
                .addMetadata(OutgoingKafkaRecordMetadata.builder()
                        .withKey(event.getOrderId().toString())
                        .build());
        
        // Send to Kafka topic: inventory-requests
        inventoryEmitter.send(message);
        
        LOG.infof("Inventory %s request published successfully for order: %s", operation, event.getOrderId());
    }
}
