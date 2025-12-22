package com.enit.orderservice.infrastructure.messaging.events;

public class PaymentResponseEvent extends BaseEvent {
    private boolean success;
    private String paymentId;
    private String errorMessage;

    public PaymentResponseEvent() {
        super();
        setEventType("PAYMENT_RESPONSE");
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
