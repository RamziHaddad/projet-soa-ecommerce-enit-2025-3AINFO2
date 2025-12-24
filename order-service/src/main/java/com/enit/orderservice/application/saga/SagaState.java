package com.enit.orderservice.application.saga;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks the state and progress of a saga execution.
 * Enables saga recovery after service restarts.
 */
@Entity
@Table(name = "saga_state")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sagaId;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStep currentStep;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(length = 1000)
    private String errorMessage;

    // Compensation tracking
    private boolean compensationRequired;
    private boolean compensationCompleted;

    // Idempotency tracking
    @Column(unique = true)
    private String idempotencyKey;

    // Retry tracking
    private int retryCount;
    private static final int MAX_RETRIES = 3;

    public boolean canRetry() {
        return retryCount < MAX_RETRIES;
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public void markCompleted() {
        this.status = SagaStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String error) {
        this.status = SagaStatus.FAILED;
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }

    public void markCompensating() {
        this.status = SagaStatus.COMPENSATING;
        this.compensationRequired = true;
    }
}