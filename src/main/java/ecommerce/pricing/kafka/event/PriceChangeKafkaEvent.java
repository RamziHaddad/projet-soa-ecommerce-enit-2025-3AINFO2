package ecommerce.pricing.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceChangeKafkaEvent {
    private Long productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changeType;
    private String reason;
    private LocalDateTime timestamp;

    // Constructeur vide (requis pour JSON)
    public PriceChangeKafkaEvent() {}

    public PriceChangeKafkaEvent(Long productId, BigDecimal oldPrice, BigDecimal newPrice,
                                 String changeType, String reason) {
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.changeType = changeType;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }

    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}