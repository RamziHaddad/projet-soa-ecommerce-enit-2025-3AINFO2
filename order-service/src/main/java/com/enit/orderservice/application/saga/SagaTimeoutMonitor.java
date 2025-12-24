package com.enit.orderservice.application.saga;


import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;

import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Monitors saga execution and handles timeout scenarios.
 * Runs periodically to detect and compensate stuck sagas.
 */
@ApplicationScoped
public class SagaTimeoutMonitor {

    private static final Logger LOG = Logger.getLogger(SagaTimeoutMonitor.class);
    private static final int SAGA_TIMEOUT_MINUTES = 10; // Configure as needed

    @Inject
    SagaStateRepository sagaStateRepository;

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderSagaOrchestrator sagaOrchestrator;

    /**
     * Runs every 5 minutes to check for stuck sagas
     */
    @Scheduled(every = "5m")
    @Transactional
    public void monitorSagaTimeouts() {
        LOG.info("Running saga timeout monitor...");

        List<SagaState> stuckSagas = sagaStateRepository.findStuckSagas(SAGA_TIMEOUT_MINUTES);

        if (stuckSagas.isEmpty()) {
            LOG.info("No stuck sagas found");
            return;
        }

        LOG.warnf("Found %d stuck sagas", stuckSagas.size());

        for (SagaState sagaState : stuckSagas) {
            try {
                handleStuckSaga(sagaState);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to handle stuck saga: %s", sagaState.getSagaId());
            }
        }
    }

    private void handleStuckSaga(SagaState sagaState) {
        LOG.warnf("Handling stuck saga for order: %s, step: %s",
                sagaState.getOrderId(), sagaState.getCurrentStep());

        Order order = orderRepository.findById(sagaState.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found for stuck saga: %s", sagaState.getOrderId());
            return;
        }

        // Check if saga can be retried
        if (sagaState.canRetry()) {
            LOG.infof("Retrying stuck saga for order: %s (attempt %d)",
                    sagaState.getOrderId(), sagaState.getRetryCount() + 1);

            sagaState.incrementRetry();
            sagaStateRepository.save(sagaState);

            // Retry from current step
            retryFromCurrentStep(sagaState, order);
        } else {
            LOG.errorf("Max retries exceeded for saga, triggering compensation: %s",
                    sagaState.getOrderId());

            // Trigger compensation
            sagaState.setStatus(SagaStatus.COMPENSATING);
            sagaState.setCompensationRequired(true);
            sagaStateRepository.save(sagaState);

            // This would call your compensation logic
            // sagaOrchestrator.compensateSaga(...);
        }
    }

    private void retryFromCurrentStep(SagaState sagaState, Order order) {
        // Implement retry logic based on current step
        // This is a simplified example
        switch (sagaState.getCurrentStep()) {
            case PRICING_REQUESTED:
                // Retry pricing request
                LOG.info("Retrying pricing request");
                break;
            case INVENTORY_REQUESTED:
                // Retry inventory request
                LOG.info("Retrying inventory request");
                break;
            // Add other steps as needed
            default:
                LOG.warnf("Cannot retry from step: %s", sagaState.getCurrentStep());
        }
    }
}