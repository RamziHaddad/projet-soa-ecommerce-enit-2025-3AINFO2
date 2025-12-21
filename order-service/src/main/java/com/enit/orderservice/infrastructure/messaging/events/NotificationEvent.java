package com.enit.orderservice.infrastructure.messaging.events;

import java.util.Map;

public class NotificationEvent extends BaseEvent {
    private String customerId;
    private NotificationType type;
    private String message;
    private Map<String, String> templateData;

    public NotificationEvent() {
        super();
        setEventType("NOTIFICATION");
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getTemplateData() {
        return templateData;
    }

    public void setTemplateData(Map<String, String> templateData) {
        this.templateData = templateData;
    }
}
