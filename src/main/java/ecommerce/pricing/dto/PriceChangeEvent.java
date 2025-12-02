package ecommerce.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceChangeEvent {
    private Long eventId;
    private Long productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changeType;
    private String reason;
    private LocalDateTime timestamp;

    public PriceChangeEvent(Long productId, BigDecimal oldPrice, BigDecimal newPrice, String changeType) {
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.changeType = changeType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public Long getEventId() { return eventId; }
    public Long getProductId() { return productId; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public String getChangeType() { return changeType; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public void setReason(String reason) { this.reason = reason; }
}