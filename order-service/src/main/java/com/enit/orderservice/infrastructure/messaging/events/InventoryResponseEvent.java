package com.enit.orderservice.infrastructure.messaging.events;

/**
 * Event representing the response from Inventory Service.
 * Indicates whether inventory was successfully reserved or released.
 */
public class InventoryResponseEvent extends BaseEvent {
    
    private boolean reserved;
    private String reservationId;
    private String errorMessage;

    public InventoryResponseEvent() {
        super();
        setEventType("INVENTORY_RESPONSE");
    }

    /**
     * @return true if inventory was successfully reserved, false otherwise
     */
    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    /**
     * @return The reservation ID for tracking (used for compensation/release)
     */
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * @return Error message if reservation failed
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
