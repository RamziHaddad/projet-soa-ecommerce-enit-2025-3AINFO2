package com.ecommerce.recommendation.dto;

import java.util.List;

public class RecommendationResponse {
    private Long userId;
    private String algorithm;
    private List<RecommendedProduct> recommendations;
    
    public RecommendationResponse() {}
    
    public RecommendationResponse(Long userId, String algorithm, List<RecommendedProduct> recommendations) {
        this.userId = userId;
        this.algorithm = algorithm;
        this.recommendations = recommendations;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public List<RecommendedProduct> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendedProduct> recommendations) { this.recommendations = recommendations; }
}