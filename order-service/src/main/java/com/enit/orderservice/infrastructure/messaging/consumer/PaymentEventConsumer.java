package com.enit.orderservice.infrastructure.messaging.consumer;

import com.enit.orderservice.application.saga.OrderSagaOrchestrator;
import com.enit.orderservice.infrastructure.exception.MessageConsumptionException;
import com.enit.orderservice.infrastructure.messaging.events.PaymentResponseEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * Consumer for payment service responses
 * Listens to payment-responses topic and forwards to saga orchestrator
 */
@ApplicationScoped
public class PaymentEventConsumer {

    private static final Logger LOG = Logger.getLogger(PaymentEventConsumer.class);

    @Inject
    OrderSagaOrchestrator sagaOrchestrator;

    /**
     * Consumes payment response events from Kafka
     * @param event The payment response containing payment status and ID
     */
    @Incoming("payment-responses")
    @Transactional
    public void onPaymentResponse(PaymentResponseEvent event) {
        LOG.infof("Received payment response for order: %s, success: %s", 
                  event.getOrderId(), event.isSuccess());
        
        try {
            sagaOrchestrator.handlePaymentResponse(event);
        } catch (Exception e) {
            LOG.errorf(e, "Error processing payment response for order: %s", event.getOrderId());
            throw new MessageConsumptionException("payment-responses", "order-service-group", e);
        }
    }
}
