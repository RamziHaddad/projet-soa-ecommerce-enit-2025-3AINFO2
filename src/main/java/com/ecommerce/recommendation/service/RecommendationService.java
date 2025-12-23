package com.ecommerce.recommendation.service;

import java.util.List;

import com.ecommerce.recommendation.algorithm.CollaborativeFiltering;
import com.ecommerce.recommendation.algorithm.ContentBasedFiltering;
import com.ecommerce.recommendation.algorithm.HybridFiltering;
import com.ecommerce.recommendation.algorithm.HistoricalBasedFiltering;
import com.ecommerce.recommendation.dto.RecommendationRequest;
import com.ecommerce.recommendation.dto.RecommendationResponse;
import com.ecommerce.recommendation.dto.RecommendedProduct;
import com.ecommerce.recommendation.entity.HistoricalRecommendation;
import com.ecommerce.recommendation.entity.Product;
import com.ecommerce.recommendation.entity.Order;

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

    @Inject
    HistoricalBasedFiltering historicalBasedFiltering;

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
            case "historical-based":
                recommendations = historicalBasedFiltering.generateRecommendations(request.getUserId(), maxResults);
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

        // Enregistrer une entrée dans historical_recommendations pour pouvoir orienter
        // les recommandations futures
        try {
            if (recommendations != null && !recommendations.isEmpty() && request.getUserId() != null) {
                // Trouver le produit le plus recommandé (par score)
                RecommendedProduct top = recommendations.stream()
                        .filter(r -> r.getProductId() != null)
                        .max((a, b) -> Double.compare(a.getScore() != null ? a.getScore() : 0.0,
                                b.getScore() != null ? b.getScore() : 0.0))
                        .orElse(recommendations.get(0));

                Long topProductId = top.getProductId();
                String category = null;
                if (topProductId != null) {
                    Product product = Product.findById(topProductId);
                    category = product != null ? product.category : null;
                }

                HistoricalRecommendation hist = new HistoricalRecommendation(request.getUserId(), category,
                        topProductId);
                hist.persist();
            }
        } catch (Exception e) {
            // Ne pas bloquer la génération de recommandations si la persistance échoue
            System.err.println("Erreur lors de l'enregistrement historique: " + e.getMessage());
        }

        return response;
    }

    public RecommendationResponse generateRecommendationsFromHistory(Long userId, Integer months,
            Integer maxResults) {
        // Si months est fourni, prendre la date actuelle et reculer de 'months' mois
        java.time.LocalDateTime since = null;
        if (months != null) {
            since = java.time.LocalDateTime.now().minusMonths(months.longValue());
        }

        java.util.List<HistoricalRecommendation> history;
        if (since != null) {
            history = HistoricalRecommendation.list("userId = ?1 and createdAt >= ?2", userId, since);
        } else {
            history = HistoricalRecommendation.list("userId", userId);
        }

        // Fréquences
        java.util.Map<String, Integer> categoryFreq = new java.util.HashMap<>();
        java.util.Map<Long, Integer> productFreq = new java.util.HashMap<>();

        for (HistoricalRecommendation h : history) {
            if (h.categoryRecommended != null)
                categoryFreq.put(h.categoryRecommended, categoryFreq.getOrDefault(h.categoryRecommended, 0) + 1);
            if (h.productRecommendedId != null)
                productFreq.put(h.productRecommendedId, productFreq.getOrDefault(h.productRecommendedId, 0) + 1);
        }

        // Compléter avec commandes
        java.util.List<Order> orders = Order.list("userId", userId);
        for (Order o : orders) {
            o.items.forEach(
                    item -> productFreq.put(item.getProductId(), productFreq.getOrDefault(item.getProductId(), 0) + 1));
        }

        java.util.Map<Long, Double> productScores = new java.util.HashMap<>();
        java.util.List<Product> allProducts = Product.listAll();

        for (Product p : allProducts) {
            double score = 0.0;
            if (p.category != null && categoryFreq.containsKey(p.category))
                score += categoryFreq.get(p.category) * 1.0;
            if (productFreq.containsKey(p.id))
                score += productFreq.get(p.id) * 2.0;
            if (score > 0)
                productScores.put(p.id, score);
        }

        java.util.List<RecommendedProduct> recommendations = productScores.entrySet().stream()
                .sorted(java.util.Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults != null ? maxResults : 10)
                .map(entry -> {
                    RecommendedProduct rp = new RecommendedProduct();
                    Product prod = Product.findById(entry.getKey());
                    rp.setProductId(entry.getKey());
                    rp.setProductName(prod != null ? prod.name : "Produit " + entry.getKey());
                    rp.setScore(entry.getValue());
                    rp.setReason("Basé sur votre historique de recommandations");
                    return rp;
                })
                .collect(java.util.stream.Collectors.toList());

        RecommendationResponse response = new RecommendationResponse();
        response.setUserId(userId);
        response.setAlgorithm("historical-based-period");
        response.setRecommendations(recommendations);

        return response;
    }

    public List<String> getAvailableAlgorithms() {
        return List.of("collaborative-filtering", "content-based", "hybrid-filtering", "historical-based");
    }
}