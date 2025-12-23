package com.enit.orderservice.infrastructure.exception;

/**
 * Thrown when consuming/processing a Kafka message fails
 */
public class MessageConsumptionException extends RuntimeException {
    
    private final String topic;
    private final String consumerGroup;
    
    public MessageConsumptionException(String topic, String consumerGroup, String reason) {
        super(String.format("Failed to consume message from topic '%s' (group: %s): %s", 
            topic, consumerGroup, reason));
        this.topic = topic;
        this.consumerGroup = consumerGroup;
    }
    
    public MessageConsumptionException(String topic, String consumerGroup, Throwable cause) {
        super(String.format("Failed to consume message from topic '%s' (group: %s)", 
            topic, consumerGroup), cause);
        this.topic = topic;
        this.consumerGroup = consumerGroup;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public String getConsumerGroup() {
        return consumerGroup;
    }
}
