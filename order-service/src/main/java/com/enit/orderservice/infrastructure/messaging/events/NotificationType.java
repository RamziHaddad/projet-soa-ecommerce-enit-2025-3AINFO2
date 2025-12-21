package com.enit.orderservice.infrastructure.messaging.events;

public enum NotificationType {
    ORDER_CONFIRMED,
    ORDER_FAILED,
    PAYMENT_SUCCESS,
    DELIVERY_CREATED,
    INVENTORY_RESERVED,
    COMPENSATION_STARTED
}
