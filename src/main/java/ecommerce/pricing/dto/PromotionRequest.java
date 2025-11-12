package ecommerce.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PromotionRequest {
    private Long productId;
    private BigDecimal discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    
    // Constructeurs
    public PromotionRequest() {}
    
    public PromotionRequest(Long productId, BigDecimal discountPercentage, 
                          LocalDate startDate, LocalDate endDate) {
        this.productId = productId;
        this.discountPercentage = discountPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters et Setters
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
}