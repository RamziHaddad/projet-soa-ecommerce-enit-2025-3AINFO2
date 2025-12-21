package com.enit.orderservice.infrastructure.messaging.events;

import java.util.List;
import java.util.UUID;

public class DeliveryCreationEvent extends BaseEvent {
    private String customerId;
    private String deliveryAddress;
    private List<UUID> productIds;

    public DeliveryCreationEvent() {
        super();
        setEventType("DELIVERY_CREATION");
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<UUID> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<UUID> productIds) {
        this.productIds = productIds;
    }
}
