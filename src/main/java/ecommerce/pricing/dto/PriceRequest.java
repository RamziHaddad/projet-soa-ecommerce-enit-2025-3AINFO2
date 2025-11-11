package ecommerce.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceRequest {
    private Long productId;
    private BigDecimal basePrice;
    private String currency;
    private LocalDate effectiveDate;
    
    // Constructors
    public PriceRequest() {}
    
    public PriceRequest(Long productId, BigDecimal basePrice) {
        this.productId = productId;
        this.basePrice = basePrice;
    }
    
    public PriceRequest(Long productId, BigDecimal basePrice, String currency) {
        this.productId = productId;
        this.basePrice = basePrice;
        this.currency = currency;
    }
    
    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}