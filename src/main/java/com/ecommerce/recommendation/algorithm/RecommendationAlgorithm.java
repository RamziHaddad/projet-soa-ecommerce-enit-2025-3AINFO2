package com.ecommerce.recommendation.algorithm;

import java.util.List;

import com.ecommerce.recommendation.dto.RecommendedProduct;

public interface RecommendationAlgorithm {
    String getName();
    List<RecommendedProduct> generateRecommendations(Long userId, Integer maxResults);
}