package com.enit.orderservice.infrastructure.outbox;

/**
 * Status of outbox events
 */
public enum OutboxStatus {
    PENDING,    // Event created, waiting to be published
    PUBLISHED,  // Successfully published to Kafka
    FAILED      // Failed to publish after retries
}
