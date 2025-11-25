package com.ecommerce.recommendation.dto;

public class RecommendedProduct {
    private Long productId;
    private String productName;
    private Double score;
    private String reason;
    
    public RecommendedProduct() {}
    
    public RecommendedProduct(Long productId, String productName, Double score, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.score = score;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}