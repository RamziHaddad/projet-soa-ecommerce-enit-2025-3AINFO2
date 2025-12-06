package ecommerce.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateType; // "PRICE" ou "PROMOTION"

    @Column(nullable = false)
    private String aggregateId; // ID du prix ou promotion

    @Column(nullable = false)
    private String eventType; // "PRICE_CHANGED" ou "PROMOTION_EXPIRING"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON de l'événement

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status; // PENDING, PROCESSED, FAILED

    @Column
    private Integer retryCount = 0;

    @Column
    private String errorMessage;

    public enum OutboxStatus {
        PENDING, PROCESSED, FAILED
    }
}