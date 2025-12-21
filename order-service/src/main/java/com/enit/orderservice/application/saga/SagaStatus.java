package com.enit.orderservice.application.saga;

public enum SagaStatus {
    IN_PROGRESS,
    COMPENSATING,
    COMPLETED,
    FAILED
}
