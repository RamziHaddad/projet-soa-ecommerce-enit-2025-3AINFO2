package com.enit.orderservice.application.saga;

import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.model.OrderStatus;
import com.enit.orderservice.domaine.repository.OrderRepository;
import com.enit.orderservice.infrastructure.messaging.producer.*;
import com.enit.orderservice.infrastructure.messaging.events.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderSagaOrchestrator {

    private static final Logger LOG = Logger.getLogger(OrderSagaOrchestrator.class);

    @Inject
    PricingEventProducer pricingEventProducer;

    @Inject
    InventoryEventProducer inventoryEventProducer;

    @Inject
    CardValidationEventProducer cardValidationEventProducer;

    @Inject
    PaymentEventProducer paymentEventProducer;

    @Inject
    DeliveryEventProducer deliveryEventProducer;

    @Inject
    NotificationEventProducer notificationEventProducer;

    @Inject
    OrderRepository orderRepository;

    /**
     * Start the saga workflow for a new order
     */
    @Transactional
    public void startSaga(UUID orderId) {
        LOG.infof("Starting saga for order: %s", orderId);
        
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", orderId);
            return;
        }

        // Step 1: Request Pricing
        requestPricing(order);
    }

    /**
     * Step 1: Request pricing from Pricing Service
     */
    private void requestPricing(Order order) {
        LOG.infof("Requesting pricing for order: %s", order.getOrderId());
        
        PricingRequestEvent event = new PricingRequestEvent();
        event.setOrderId(order.getOrderId());
        event.setItems(order.getItems());
        
        pricingEventProducer.publishRequest(event);
    }

    /**
     * Handle pricing response
     */
    @Transactional
    public void handlePricingResponse(PricingResponseEvent event) {
        LOG.infof("Handling pricing response for order: %s, status: %s", 
                  event.getOrderId(), event.getStatus());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        if (event.getStatus() == EventStatus.SUCCESS) {
            // Update order with total price
            order.setTotalMoney(event.getTotalPrice());
            orderRepository.save(order);
            
            // Step 2: Reserve Inventory
            requestInventoryReservation(order);
        } else {
            failSaga(order, "Pricing failed: " + event.getErrorMessage());
        }
    }

    /**
     * Step 2: Request inventory reservation
     */
    private void requestInventoryReservation(Order order) {
        LOG.infof("Requesting inventory reservation for order: %s", order.getOrderId());
        
        InventoryRequestEvent event = new InventoryRequestEvent();
        event.setOrderId(order.getOrderId());
        event.setItems(order.getItems());
        event.setRelease(false); // Reserve operation
        
        inventoryEventProducer.publishRequest(event);
    }

    /**
     * Handle inventory response
     */
    @Transactional
    public void handleInventoryResponse(InventoryResponseEvent event) {
        LOG.infof("Handling inventory response for order: %s, reserved: %s", 
                  event.getOrderId(), event.isReserved());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        if (event.isReserved()) {
            // Save reservation ID for compensation
            order.setInventoryReservationId(event.getReservationId());
            orderRepository.save(order);
            
            // Step 3: Validate Card
            requestCardValidation(order);
        } else {
            compensateSaga(order, "Inventory reservation failed: " + event.getErrorMessage());
        }
    }

    /**
     * Step 3: Request card validation
     */
    private void requestCardValidation(Order order) {
        LOG.infof("Requesting card validation for order: %s", order.getOrderId());
        
        // Note: Card details should come from order request
        // This is a placeholder - you'll need to add card info to Order entity
        CardValidationRequestEvent event = new CardValidationRequestEvent();
        event.setOrderId(order.getOrderId());
        // event.setCardNumber(...);
        // event.setCardHolderName(...);
        // event.setExpiryDate(...);
        // event.setCvv(...);
        
        cardValidationEventProducer.publishRequest(event);
    }

    /**
     * Handle card validation response
     */
    @Transactional
    public void handleCardValidationResponse(CardValidationResponseEvent event) {
        LOG.infof("Handling card validation response for order: %s, valid: %s", 
                  event.getOrderId(), event.isValid());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        if (event.isValid()) {
            // Step 4: Process Payment
            requestPayment(order, event.getCardToken());
        } else {
            compensateSaga(order, "Card validation failed: " + event.getErrorMessage());
        }
    }

    /**
     * Step 4: Request payment processing
     */
    private void requestPayment(Order order, String cardToken) {
        LOG.infof("Requesting payment for order: %s", order.getOrderId());
        
        PaymentRequestEvent event = new PaymentRequestEvent();
        event.setOrderId(order.getOrderId());
        event.setCardToken(cardToken);
        event.setAmount(order.getTotalMoney());
        event.setRefund(false); // Payment operation
        
        paymentEventProducer.publishRequest(event);
    }

    /**
     * Handle payment response
     */
    @Transactional
    public void handlePaymentResponse(PaymentResponseEvent event) {
        LOG.infof("Handling payment response for order: %s, success: %s", 
                  event.getOrderId(), event.isSuccess());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        if (event.isSuccess()) {
            // Save payment ID for compensation
            order.setPaymentId(event.getPaymentId());
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            
            // Step 5: Create Delivery (async - fire and forget)
            requestDeliveryCreation(order);
            
            // Step 6: Send notification
            sendNotification(order, NotificationType.ORDER_CONFIRMED);
            
            // Complete saga
            completeSaga(order);
        } else {
            compensateSaga(order, "Payment failed: " + event.getErrorMessage());
        }
    }

    /**
     * Step 5: Request delivery creation (Kafka - async)
     */
    private void requestDeliveryCreation(Order order) {
        LOG.infof("Requesting delivery creation for order: %s", order.getOrderId());
        
        DeliveryCreationEvent event = new DeliveryCreationEvent();
        event.setOrderId(order.getOrderId());
        event.setCustomerId(order.getCustomerId());
        /*event.setDeliveryAddress(order.getDeliveryAddress());
        
        // Extract product IDs from order items
        List<UUID> productIds = order.getItems().stream()
                .map(item -> item.getProductId())
                .collect(Collectors.toList());
        event.setProductIds(productIds);*/
        
        deliveryEventProducer.publishRequest(event);
    }

    /**
     * Handle delivery response (optional - for tracking)
     */
    @Transactional
    public void handleDeliveryResponse(DeliveryResponseEvent event) {
        LOG.infof("Delivery created for order: %s, tracking: %s", 
                  event.getOrderId(), event.getTrackingNumber());
        
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order != null) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }
    }

    /**
     * Step 6: Send notification (Kafka - fire and forget)
     */
    private void sendNotification(Order order, NotificationType type) {
        LOG.infof("Sending notification for order: %s, type: %s", order.getOrderId(), type);
        
        NotificationEvent event = new NotificationEvent();
        event.setOrderId(order.getOrderId());
        event.setCustomerId(order.getCustomerId());
        event.setType(type);
        event.setMessage("Order " + order.getOrderId() + " - " + type);
        
        notificationEventProducer.publishNotification(event);
    }

    /**
     * Complete saga successfully
     */
    @Transactional
    private void completeSaga(Order order) {
        LOG.infof("Completing saga for order: %s", order.getOrderId());
        
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
        
        LOG.infof("Saga completed successfully for order: %s", order.getOrderId());
    }

    /**
     * Fail saga immediately (no compensation needed)
     */
    @Transactional
    private void failSaga(Order order, String reason) {
        LOG.errorf("Failing saga for order: %s, reason: %s", order.getOrderId(), reason);
        
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        
        // Send failure notification
        sendNotification(order, NotificationType.ORDER_FAILED);
        
        LOG.infof("Saga failed for order: %s", order.getOrderId());
    }

    /**
     * Compensate saga (rollback transactions)
     */
    @Transactional
    private void compensateSaga(Order order, String reason) {
        LOG.warnf("Compensating saga for order: %s, reason: %s", order.getOrderId(), reason);
        
        // Send compensation notification
        sendNotification(order, NotificationType.COMPENSATION_STARTED);

        // Compensation logic: Rollback in reverse order
        
        // 1. Refund payment if processed
        if (order.getPaymentId() != null) {
            LOG.infof("Refunding payment for order: %s", order.getOrderId());
            
            PaymentRequestEvent refundEvent = new PaymentRequestEvent();
            refundEvent.setOrderId(order.getOrderId());
            refundEvent.setPaymentId(order.getPaymentId());
            refundEvent.setRefund(true); // Refund operation
            
            paymentEventProducer.publishRequest(refundEvent);
        }

        // 2. Release inventory if reserved
        if (order.getInventoryReservationId() != null) {
            LOG.infof("Releasing inventory for order: %s", order.getOrderId());
            
            InventoryRequestEvent releaseEvent = new InventoryRequestEvent();
            releaseEvent.setOrderId(order.getOrderId());
            releaseEvent.setItems(order.getItems());
            releaseEvent.setRelease(true); // Release operation
            
            inventoryEventProducer.publishRequest(releaseEvent);
        }

        // 3. Mark order as failed
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        
        // Send final failure notification
        sendNotification(order, NotificationType.ORDER_FAILED);
        
        LOG.infof("Compensation completed for order: %s", order.getOrderId());
    }
}
