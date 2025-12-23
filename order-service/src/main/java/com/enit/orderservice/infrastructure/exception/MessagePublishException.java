package com.enit.orderservice.infrastructure.exception;

/**
 * Thrown when publishing a message to Kafka fails
 */
public class MessagePublishException extends RuntimeException {
    
    private final String topic;
    private final Object message;
    
    public MessagePublishException(String topic, Object message, String reason) {
        super(String.format("Failed to publish message to topic '%s': %s", topic, reason));
        this.topic = topic;
        this.message = message;
    }
    
    public MessagePublishException(String topic, Object message, Throwable cause) {
        super(String.format("Failed to publish message to topic '%s'", topic), cause);
        this.topic = topic;
        this.message = message;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public Object getMessageObject() {
        return message;
    }
}
