package ecommerce.pricing.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PromotionExpiringKafkaEvent {
    private Long promotionId;
    private Long productId;
    private BigDecimal discountPercentage;
    private LocalDate endDate;
    private Integer daysRemaining;
    private String description;

    public PromotionExpiringKafkaEvent() {}

    public PromotionExpiringKafkaEvent(Long promotionId, Long productId,
                                       BigDecimal discountPercentage, LocalDate endDate,
                                       Integer daysRemaining, String description) {
        this.promotionId = promotionId;
        this.productId = productId;
        this.discountPercentage = discountPercentage;
        this.endDate = endDate;
        this.daysRemaining = daysRemaining;
        this.description = description;
    }

    // Getters et Setters
    public Long getPromotionId() { return promotionId; }
    public void setPromotionId(Long promotionId) { this.promotionId = promotionId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(Integer daysRemaining) { this.daysRemaining = daysRemaining; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}