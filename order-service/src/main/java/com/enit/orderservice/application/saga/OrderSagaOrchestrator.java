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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderSagaOrchestrator {

    private static final Logger LOG = Logger.getLogger(OrderSagaOrchestrator.class);

    @Inject PricingEventProducer pricingEventProducer;
    @Inject InventoryEventProducer inventoryEventProducer;
    @Inject CardValidationEventProducer cardValidationEventProducer;
    @Inject PaymentEventProducer paymentEventProducer;
    @Inject DeliveryEventProducer deliveryEventProducer;
    @Inject NotificationEventProducer notificationEventProducer;
    @Inject OrderRepository orderRepository;
    @Inject SagaStateRepository sagaStateRepository;

    /**
     * Start saga workflow with state tracking and idempotency
     */
    @Transactional
    public void startSaga(UUID orderId) {
        LOG.infof("Starting saga for order: %s", orderId);

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", orderId);
            return;
        }

        // Check if saga already exists (idempotency)
        String idempotencyKey = "order-" + orderId.toString();
        if (sagaStateRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            LOG.warnf("Saga already exists for order: %s", orderId);
            return;
        }

        // Create saga state
        SagaState sagaState = SagaState.builder()
                .orderId(orderId)
                .status(SagaStatus.IN_PROGRESS)
                .currentStep(SagaStep.ORDER_CREATED)
                .startedAt(LocalDateTime.now())
                .idempotencyKey(idempotencyKey)
                .retryCount(0)
                .build();
        sagaStateRepository.save(sagaState);

        // Start saga flow
        requestPricing(order, sagaState);
    }

//    private void requestPricing(Order order) {
//        SagaState sagaState = getSagaState(order.getOrderId());
//        requestPricing(order, sagaState);
//    }

    private void requestPricing(Order order, SagaState sagaState) {
        LOG.infof("Requesting pricing for order: %s", order.getOrderId());

        sagaState.setCurrentStep(SagaStep.PRICING_REQUESTED);
        sagaStateRepository.save(sagaState);

        PricingRequestEvent event = new PricingRequestEvent();
        event.setOrderId(order.getOrderId());
        event.setItems(order.getItems());

        pricingEventProducer.publishRequest(event);
    }

    @Transactional
    public void handlePricingResponse(PricingResponseEvent event) {
        LOG.infof("Handling pricing response for order: %s, status: %s",
                event.getOrderId(), event.getStatus());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        SagaState sagaState = getSagaState(event.getOrderId());

        if (event.getStatus() == EventStatus.SUCCESS) {
            order.setTotalMoney(event.getTotalPrice());
            orderRepository.save(order);

            sagaState.setCurrentStep(SagaStep.PRICING_COMPLETED);
            sagaStateRepository.save(sagaState);

            requestInventoryReservation(order, sagaState);
        } else {
            failSaga(order, sagaState, "Pricing failed: " + event.getErrorMessage());
        }
    }

    private void requestInventoryReservation(Order order, SagaState sagaState) {
        LOG.infof("Requesting inventory reservation for order: %s", order.getOrderId());

        sagaState.setCurrentStep(SagaStep.INVENTORY_REQUESTED);
        sagaStateRepository.save(sagaState);

        InventoryRequestEvent event = new InventoryRequestEvent();
        event.setOrderId(order.getOrderId());
        event.setItems(order.getItems());
        event.setRelease(false);

        inventoryEventProducer.publishRequest(event);
    }

    @Transactional
    public void handleInventoryResponse(InventoryResponseEvent event) {
        LOG.infof("Handling inventory response for order: %s, reserved: %s",
                event.getOrderId(), event.isReserved());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        SagaState sagaState = getSagaState(event.getOrderId());

        if (event.isReserved()) {
            order.setInventoryReservationId(event.getReservationId());
            orderRepository.save(order);

            sagaState.setCurrentStep(SagaStep.INVENTORY_RESERVED);
            sagaStateRepository.save(sagaState);

            requestCardValidation(order, sagaState);
        } else {
            compensateSaga(order, sagaState, "Inventory reservation failed: " + event.getErrorMessage());
        }
    }

    private void requestCardValidation(Order order, SagaState sagaState) {
        LOG.infof("Requesting card validation for order: %s", order.getOrderId());

        sagaState.setCurrentStep(SagaStep.CARD_VALIDATION_REQUESTED);
        sagaStateRepository.save(sagaState);

        CardValidationRequestEvent event = new CardValidationRequestEvent();
        event.setOrderId(order.getOrderId());
        event.setCardNumber(order.getCardNumber());
        event.setCardHolderName(order.getCardHolderName());
        event.setExpiryDate(order.getExpiryDate());
        event.setCvv(order.getCvv());

        cardValidationEventProducer.publishRequest(event);
    }

    @Transactional
    public void handleCardValidationResponse(CardValidationResponseEvent event) {
        LOG.infof("Handling card validation response for order: %s, valid: %s",
                event.getOrderId(), event.isValid());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        SagaState sagaState = getSagaState(event.getOrderId());

        if (event.isValid()) {
            sagaState.setCurrentStep(SagaStep.CARD_VALIDATED);
            sagaStateRepository.save(sagaState);

            requestPayment(order, sagaState, event.getCardToken());
        } else {
            compensateSaga(order, sagaState, "Card validation failed: " + event.getErrorMessage());
        }
    }

    private void requestPayment(Order order, SagaState sagaState, String cardToken) {
        LOG.infof("Requesting payment for order: %s", order.getOrderId());

        sagaState.setCurrentStep(SagaStep.PAYMENT_REQUESTED);
        sagaStateRepository.save(sagaState);

        PaymentRequestEvent event = new PaymentRequestEvent();
        event.setOrderId(order.getOrderId());
        event.setCardToken(cardToken);
        event.setAmount(order.getTotalMoney());
        event.setRefund(false);

        paymentEventProducer.publishRequest(event);
    }

    @Transactional
    public void handlePaymentResponse(PaymentResponseEvent event) {
        LOG.infof("Handling payment response for order: %s, success: %s",
                event.getOrderId(), event.isSuccess());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            LOG.errorf("Order not found: %s", event.getOrderId());
            return;
        }

        SagaState sagaState = getSagaState(event.getOrderId());

        if (event.isSuccess()) {
            order.setPaymentId(event.getPaymentId());
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            sagaState.setCurrentStep(SagaStep.PAYMENT_PROCESSED);
            sagaStateRepository.save(sagaState);

            requestDeliveryCreation(order, sagaState);
            sendNotification(order, NotificationType.ORDER_CONFIRMED);
            completeSaga(order, sagaState);
        } else {
            compensateSaga(order, sagaState, "Payment failed: " + event.getErrorMessage());
        }
    }

    private void requestDeliveryCreation(Order order, SagaState sagaState) {
        LOG.infof("Requesting delivery creation for order: %s", order.getOrderId());

        sagaState.setCurrentStep(SagaStep.DELIVERY_REQUESTED);
        sagaStateRepository.save(sagaState);

        DeliveryCreationEvent event = new DeliveryCreationEvent();
        event.setOrderId(order.getOrderId());
        event.setCustomerId(order.getCustomerId());
        event.setDeliveryAddress(order.getDeliveryAddress());

        List<UUID> productIds = order.getItems().stream()
                .map(item -> item.getProductId())
                .collect(Collectors.toList());
        event.setProductIds(productIds);

        deliveryEventProducer.publishRequest(event);
    }

    @Transactional
    public void handleDeliveryResponse(DeliveryResponseEvent event) {
        LOG.infof("Delivery created for order: %s, tracking: %s",
                event.getOrderId(), event.getTrackingNumber());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order != null) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);

            SagaState sagaState = getSagaState(event.getOrderId());
            sagaState.setCurrentStep(SagaStep.DELIVERY_CREATED);
            sagaStateRepository.save(sagaState);
        }
    }

    private void sendNotification(Order order, NotificationType type) {
        LOG.infof("Sending notification for order: %s, type: %s", order.getOrderId(), type);

        NotificationEvent event = new NotificationEvent();
        event.setOrderId(order.getOrderId());
        event.setCustomerId(order.getCustomerId());
        event.setType(type);
        event.setMessage("Order " + order.getOrderId() + " - " + type);

        notificationEventProducer.publishNotification(event);
    }

    @Transactional
    private void completeSaga(Order order, SagaState sagaState) {
        LOG.infof("Completing saga for order: %s", order.getOrderId());

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        sagaState.setCurrentStep(SagaStep.ORDER_COMPLETED);
        sagaState.markCompleted();
        sagaStateRepository.save(sagaState);

        LOG.infof("Saga completed successfully for order: %s", order.getOrderId());
    }

    @Transactional
    private void failSaga(Order order, SagaState sagaState, String reason) {
        LOG.errorf("Failing saga for order: %s, reason: %s", order.getOrderId(), reason);

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        sagaState.setCurrentStep(SagaStep.ORDER_FAILED);
        sagaState.markFailed(reason);
        sagaStateRepository.save(sagaState);

        sendNotification(order, NotificationType.ORDER_FAILED);

        LOG.infof("Saga failed for order: %s", order.getOrderId());
    }

    @Transactional
    private void compensateSaga(Order order, SagaState sagaState, String reason) {
        LOG.warnf("Compensating saga for order: %s, reason: %s", order.getOrderId(), reason);

        sagaState.setCurrentStep(SagaStep.COMPENSATION_STARTED);
        sagaState.markCompensating();
        sagaStateRepository.save(sagaState);

        sendNotification(order, NotificationType.COMPENSATION_STARTED);

        if (order.getPaymentId() != null) {
            LOG.infof("Refunding payment for order: %s", order.getOrderId());

            PaymentRequestEvent refundEvent = new PaymentRequestEvent();
            refundEvent.setOrderId(order.getOrderId());
            refundEvent.setPaymentId(order.getPaymentId());
            refundEvent.setRefund(true);

            paymentEventProducer.publishRequest(refundEvent);
        }

        if (order.getInventoryReservationId() != null) {
            LOG.infof("Releasing inventory for order: %s", order.getOrderId());

            InventoryRequestEvent releaseEvent = new InventoryRequestEvent();
            releaseEvent.setOrderId(order.getOrderId());
            releaseEvent.setItems(order.getItems());
            releaseEvent.setRelease(true);

            inventoryEventProducer.publishRequest(releaseEvent);
        }

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        sagaState.setCompensationCompleted(true);
        sagaState.markFailed(reason);
        sagaStateRepository.save(sagaState);

        sendNotification(order, NotificationType.ORDER_FAILED);

        LOG.infof("Compensation completed for order: %s", order.getOrderId());
    }

    private SagaState getSagaState(UUID orderId) {
        return sagaStateRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Saga state not found for order: " + orderId));
    }
}