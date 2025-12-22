package com.enit.orderservice.infrastructure.messaging.events;

import com.enit.orderservice.domaine.model.OrderItem;

import java.util.List;

public class PricingRequestEvent extends BaseEvent {
    private List<OrderItem> items;

    public PricingRequestEvent() {
        super();
        setEventType("PRICING_REQUEST");
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
