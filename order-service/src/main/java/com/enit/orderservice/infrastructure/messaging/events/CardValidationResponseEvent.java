package com.enit.orderservice.infrastructure.messaging.events;

public class CardValidationResponseEvent extends BaseEvent {
    private boolean valid;
    private String cardToken;
    private String errorMessage;

    public CardValidationResponseEvent() {
        super();
        setEventType("CARD_VALIDATION_RESPONSE");
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
