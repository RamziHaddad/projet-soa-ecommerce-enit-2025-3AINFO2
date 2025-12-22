package com.enit.orderservice.infrastructure.messaging.events;

import java.math.BigDecimal;

public class PaymentRequestEvent extends BaseEvent {
    private String cardToken;
    private BigDecimal amount;
    private boolean refund;
    private String paymentId; // For refund operations

    public PaymentRequestEvent() {
        super();
        setEventType("PAYMENT_REQUEST");
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isRefund() {
        return refund;
    }

    public void setRefund(boolean refund) {
        this.refund = refund;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
