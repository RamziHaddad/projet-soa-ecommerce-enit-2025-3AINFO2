package com.enit.orderservice.infrastructure.messaging.consumer;

import com.enit.orderservice.application.saga.OrderSagaOrchestrator;
import com.enit.orderservice.infrastructure.exception.MessageConsumptionException;
import com.enit.orderservice.infrastructure.messaging.events.CardValidationResponseEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Consumer for card validation service responses
 * Listens to card-validation-responses topic and forwards to saga orchestrator
 */
@ApplicationScoped
public class CardValidationEventConsumer {

    private static final Logger LOG = Logger.getLogger(CardValidationEventConsumer.class);

    @Inject
    OrderSagaOrchestrator sagaOrchestrator;

    /**
     * Consumes card validation response events from Kafka
     * @param event The card validation response containing validation result and token
     */
    @Incoming("card-validation-responses")
    @Transactional
    public void onCardValidationResponse(CardValidationResponseEvent event) {
        LOG.infof("Received card validation response for order: %s, valid: %s", 
                  event.getOrderId(), event.isValid());
        
        try {
            sagaOrchestrator.handleCardValidationResponse(event);
        } catch (Exception e) {
            LOG.errorf(e, "Error processing card validation response for order: %s", event.getOrderId());
            throw new MessageConsumptionException("card-validation-responses", "order-service-group", e);
        }
    }
}
