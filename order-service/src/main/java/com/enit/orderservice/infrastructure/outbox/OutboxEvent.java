package com.enit.orderservice.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbox Pattern Entity
 * Stores events that need to be published to Kafka
 * Ensures transactional consistency between database and message broker
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_status_created", columnList = "status,created_at"),
    @Index(name = "idx_aggregate_id", columnList = "aggregate_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId; // Order ID

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType; // "Order"

    @Column(name = "event_type", nullable = false)
    private String eventType; // "PricingRequest", "InventoryRequest", etc.

    @Column(name = "topic", nullable = false)
    private String topic; // Kafka topic name

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON serialized event

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OutboxStatus.PENDING;
        }
        if (retryCount == 0) {
            retryCount = 0;
        }
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = error;
        this.retryCount++;
    }

    public boolean canRetry(int maxRetries) {
        return this.retryCount < maxRetries && 
               this.status == OutboxStatus.FAILED;
    }
}
