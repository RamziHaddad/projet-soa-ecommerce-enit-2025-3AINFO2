package org.com.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbox_events")
public class InboxEvent {

   @Id
    public UUID id;

    @Column(name = "event_id", unique = true, nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID aggregateId; // ID du produit
    
    @Column(nullable = false)
    private String eventType; 

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public InboxEvent() {
        this.processedAt = LocalDateTime.now();
    }

    public InboxEvent(UUID eventId, String eventType, UUID aggregateId) {
        this();
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getAggregateId() { return aggregateId; }
    public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
