package ecommerce.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PromotionResponse {
    private Long id;
    private Long productId;
    private BigDecimal discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Boolean isActive;
    
    // Constructeurs
    public PromotionResponse() {}
    
    public PromotionResponse(Long id, Long productId, BigDecimal discountPercentage,
                           LocalDate startDate, LocalDate endDate, Boolean isActive) {
        this.id = id;
        this.productId = productId;
        this.discountPercentage = discountPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}