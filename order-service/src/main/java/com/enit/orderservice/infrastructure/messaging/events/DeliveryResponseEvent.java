package com.enit.orderservice.infrastructure.messaging.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeliveryResponseEvent extends BaseEvent {
    private UUID deliveryId;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;

    public DeliveryResponseEvent() {
        super();
        setEventType("DELIVERY_RESPONSE");
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }
}
