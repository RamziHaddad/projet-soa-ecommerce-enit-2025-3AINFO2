package com.enit.orderservice.infrastructure.messaging.events;

import java.math.BigDecimal;

public class PricingResponseEvent extends BaseEvent {
    private BigDecimal totalPrice;
    private String errorMessage;

    public PricingResponseEvent() {
        super();
        setEventType("PRICING_RESPONSE");
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
