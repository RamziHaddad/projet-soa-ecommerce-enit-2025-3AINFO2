package ecommerce.pricing.dto;

import java.math.BigDecimal;

public class PriceResponse {
    private Long productId;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private BigDecimal discountAmount;
    private String currency;
    private Boolean hasPromotion;
    
    // Constructors
    public PriceResponse() {}
    
    public PriceResponse(Long productId, BigDecimal basePrice, BigDecimal finalPrice, String currency) {
        this.productId = productId;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
        this.currency = currency;
        this.hasPromotion = !basePrice.equals(finalPrice);
        this.discountAmount = basePrice.subtract(finalPrice);
    }
    
    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Boolean getHasPromotion() { return hasPromotion; }
    public void setHasPromotion(Boolean hasPromotion) { this.hasPromotion = hasPromotion; }
}