package com.ecommerce.recommendation.service;

import java.util.List;

import com.ecommerce.recommendation.algorithm.CollaborativeFiltering;
import com.ecommerce.recommendation.algorithm.ContentBasedFiltering;
import com.ecommerce.recommendation.algorithm.HybridFiltering;
import com.ecommerce.recommendation.dto.RecommendationRequest;
import com.ecommerce.recommendation.dto.RecommendationResponse;
import com.ecommerce.recommendation.dto.RecommendedProduct;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RecommendationService {
    
    @Inject
    CollaborativeFiltering collaborativeFiltering;
    
    @Inject
    ContentBasedFiltering contentBasedFiltering;
    
    @Inject
    HybridFiltering hybridFiltering;
    
    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        String algorithmName = request.getAlgorithm() != null ? request.getAlgorithm() : "hybrid-filtering";
        Integer maxResults = request.getMaxResults() != null ? request.getMaxResults() : 10;
        
        List<RecommendedProduct> recommendations;
        
        switch (algorithmName) {
            case "content-based":
                recommendations = contentBasedFiltering.generateRecommendations(request.getUserId(), maxResults);
                break;
            case "collaborative-filtering":
                recommendations = collaborativeFiltering.generateRecommendations(request.getUserId(), maxResults);
                break;
            case "hybrid-filtering":
            default:
                recommendations = hybridFiltering.generateRecommendations(request.getUserId(), maxResults);
                break;
        }
        
        RecommendationResponse response = new RecommendationResponse();
        response.setUserId(request.getUserId());
        response.setAlgorithm(algorithmName);
        response.setRecommendations(recommendations);
        
        return response;
    }
    
    public List<String> getAvailableAlgorithms() {
        return List.of("collaborative-filtering", "content-based", "hybrid-filtering");
    }
}