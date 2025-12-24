package com.enit.orderservice.application.saga;


import com.enit.orderservice.infrastructure.exception.PersistenceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SagaStateRepositoryImpl implements SagaStateRepository {

    private static final Logger LOG = Logger.getLogger(SagaStateRepositoryImpl.class);

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public SagaState save(SagaState sagaState) {
        try {
            if (sagaState.getSagaId() == null) {
                entityManager.persist(sagaState);
                LOG.infof("Persisted new saga state: %s", sagaState.getSagaId());
                return sagaState;
            } else {
                SagaState merged = entityManager.merge(sagaState);
                LOG.infof("Updated saga state: %s", sagaState.getSagaId());
                return merged;
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save saga state: %s", sagaState.getSagaId());
            throw new PersistenceException("save", "SagaState", e);
        }
    }

    @Override
    public Optional<SagaState> findById(UUID sagaId) {
        try {
            SagaState sagaState = entityManager.find(SagaState.class, sagaId);
            return Optional.ofNullable(sagaState);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to find saga state by id: %s", sagaId);
            throw new PersistenceException("findById", "SagaState", e);
        }
    }

    @Override
    public Optional<SagaState> findByOrderId(UUID orderId) {
        try {
            List<SagaState> results = entityManager
                    .createQuery("SELECT s FROM SagaState s WHERE s.orderId = :orderId", SagaState.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to find saga state by order id: %s", orderId);
            throw new PersistenceException("findByOrderId", "SagaState", e);
        }
    }

    @Override
    public Optional<SagaState> findByIdempotencyKey(String idempotencyKey) {
        try {
            List<SagaState> results = entityManager
                    .createQuery("SELECT s FROM SagaState s WHERE s.idempotencyKey = :key", SagaState.class)
                    .setParameter("key", idempotencyKey)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to find saga state by idempotency key: %s", idempotencyKey);
            throw new PersistenceException("findByIdempotencyKey", "SagaState", e);
        }
    }

    @Override
    public List<SagaState> findByStatus(SagaStatus status) {
        try {
            return entityManager
                    .createQuery("SELECT s FROM SagaState s WHERE s.status = :status", SagaState.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to find saga states by status: %s", status);
            throw new PersistenceException("findByStatus", "SagaState", e);
        }
    }

    @Override
    public List<SagaState> findStuckSagas(int timeoutMinutes) {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
            return entityManager
                    .createQuery(
                            "SELECT s FROM SagaState s WHERE s.status = :status " +
                                    "AND s.startedAt < :threshold",
                            SagaState.class)
                    .setParameter("status", SagaStatus.IN_PROGRESS)
                    .setParameter("threshold", timeoutThreshold)
                    .getResultList();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to find stuck sagas");
            throw new PersistenceException("findStuckSagas", "SagaState", e);
        }
    }

    @Override
    @Transactional
    public void delete(SagaState sagaState) {
        try {
            if (!entityManager.contains(sagaState)) {
                sagaState = entityManager.merge(sagaState);
            }
            entityManager.remove(sagaState);
            LOG.infof("Deleted saga state: %s", sagaState.getSagaId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to delete saga state: %s", sagaState.getSagaId());
            throw new PersistenceException("delete", "SagaState", e);
        }
    }
}