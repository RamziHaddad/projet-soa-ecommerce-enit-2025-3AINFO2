package com.enit.orderservice.infrastructure.messaging.events;

import com.enit.orderservice.domaine.model.OrderItem;

import java.util.List;

public class InventoryRequestEvent extends BaseEvent {
    private List<OrderItem> items;
    private boolean release;  // false = reserve, true = release instead of making  two events : InventoryReserveEvent and InventoryReleaseEvent

    public InventoryRequestEvent() {
        super();
        setEventType("INVENTORY_REQUEST");
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public boolean isRelease() {
        return release;
    }

    public void setRelease(boolean release) {
        this.release = release;
    }
}
