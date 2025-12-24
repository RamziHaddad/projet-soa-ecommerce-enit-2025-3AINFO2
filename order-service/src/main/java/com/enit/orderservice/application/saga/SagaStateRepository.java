package com.enit.orderservice.application.saga;

import com.enit.orderservice.application.saga.SagaStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaStateRepository {

    SagaState save(SagaState sagaState);

    Optional<SagaState> findById(UUID sagaId);

    Optional<SagaState> findByOrderId(UUID orderId);

    Optional<SagaState> findByIdempotencyKey(String idempotencyKey);

    List<SagaState> findByStatus(SagaStatus status);

    /**
     * Find sagas that have been in progress for too long (timeout detection)
     */
    List<SagaState> findStuckSagas(int timeoutMinutes);

    void delete(SagaState sagaState);
}