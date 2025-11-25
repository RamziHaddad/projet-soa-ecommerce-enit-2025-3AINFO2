package com.ecommerce.recommendation.dto;

public class RecommendationRequest {
    private Long userId;
    private String algorithm;
    private Integer maxResults;
    
    public RecommendationRequest() {}
    
    public RecommendationRequest(Long userId, String algorithm, Integer maxResults) {
        this.userId = userId;
        this.algorithm = algorithm;
        this.maxResults = maxResults;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }
}