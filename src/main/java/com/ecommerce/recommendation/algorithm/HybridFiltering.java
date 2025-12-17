package com.ecommerce.recommendation.algorithm;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ecommerce.recommendation.dto.RecommendedProduct;
import com.ecommerce.recommendation.entity.Order;
import com.ecommerce.recommendation.entity.Product;
import com.ecommerce.recommendation.entity.Rating;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HybridFiltering implements RecommendationAlgorithm {
    
    @Inject
    CollaborativeFiltering collaborativeFiltering;
    
    @Inject
    ContentBasedFiltering contentBasedFiltering;
    
    @Override
    public String getName() {
        return "hybrid-filtering";
    }
    
    @Override
    public List<RecommendedProduct> generateRecommendations(Long userId, Integer maxResults) {
        System.out.println("Génération de recommandations hybrides pour: " + userId);
        
        // 1. Récupérer les recommandations des deux algorithmes
        List<RecommendedProduct> collaborativeRecs = collaborativeFiltering.generateRecommendations(userId, maxResults * 2);
        List<RecommendedProduct> contentBasedRecs = contentBasedFiltering.generateRecommendations(userId, maxResults * 2);
        
        // 2. Calculer le score d'hybridation
        Map<Long, Double> hybridScores = calculateHybridScores(collaborativeRecs, contentBasedRecs);
        
        // 3. Récupérer l'historique de l'utilisateur pour personnalisation
        Map<Long, Double> personalizedScores = personalizeScores(userId, hybridScores);
        
        System.out.println("Scores hybrides personnalisés: " + personalizedScores);
        
        // 4. Trier et retourner les recommandations
        return personalizedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> createRecommendedProduct(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
    
    private Map<Long, Double> calculateHybridScores(List<RecommendedProduct> collaborativeRecs, 
                                                    List<RecommendedProduct> contentBasedRecs) {
        Map<Long, Double> scores = new HashMap<>();
        
        // Pondération: 60% collaborative, 40% content-based
        double collaborativeWeight = 0.6;
        double contentBasedWeight = 0.4;
        
        // Normaliser et combiner les scores
        addNormalizedScores(scores, collaborativeRecs, collaborativeWeight, "collaborative");
        addNormalizedScores(scores, contentBasedRecs, contentBasedWeight, "content");
        
        return scores;
    }
    
    private void addNormalizedScores(Map<Long, Double> scores, 
                                     List<RecommendedProduct> recommendations, 
                                     double weight, 
                                     String algorithmType) {
        if (recommendations.isEmpty()) return;
        
        // Trouver le score maximum pour la normalisation
        double maxScore = recommendations.stream()
                .mapToDouble(RecommendedProduct::getScore)
                .max()
                .orElse(1.0);
        
        // Ajouter les scores normalisés avec pondération
        for (RecommendedProduct rec : recommendations) {
            double normalizedScore = (maxScore > 0) ? rec.getScore() / maxScore : rec.getScore();
            double weightedScore = normalizedScore * weight;
            
            scores.merge(rec.getProductId(), weightedScore, Double::sum);
            
            // Bonus si le produit apparaît dans les deux listes (consensus)
            if (scores.containsKey(rec.getProductId()) && 
                scores.get(rec.getProductId()) > weightedScore) {
                scores.put(rec.getProductId(), scores.get(rec.getProductId()) + 0.1);
            }
        }
    }
    
    private Map<Long, Double> personalizeScores(Long userId, Map<Long, Double> hybridScores) {
        Map<Long, Double> personalizedScores = new HashMap<>(hybridScores);
        
        // 1. Vérifier les préférences de catégorie de l'utilisateur
        List<Order> userOrders = Order.list("userId", userId);
        List<Rating> userRatings = Rating.list("userId", userId);
        
        Set<String> preferredCategories = getPreferredCategories(userOrders, userRatings);
        
        // 2. Appliquer un boost pour les produits des catégories préférées
        for (Map.Entry<Long, Double> entry : hybridScores.entrySet()) {
            Product product = Product.findById(entry.getKey());
            if (product != null && preferredCategories.contains(product.category)) {
                double boostedScore = entry.getValue() * 1.2; // 20% de boost
                personalizedScores.put(entry.getKey(), boostedScore);
            }
        }
        
        // 3. Démocratiser les recommandations (éviter les doublons)
        diversifyRecommendations(personalizedScores);
        
        return personalizedScores;
    }
    
    private Set<String> getPreferredCategories(List<Order> orders, List<Rating> ratings) {
        Set<String> categories = new HashSet<>();
        
        // Catégories des produits achetés
        for (Order order : orders) {
            for (var item : order.items) {
                Product product = Product.findById(item.getProductId());
                if (product != null && product.category != null) {
                    categories.add(product.category);
                }
            }
        }
        
        // Catégories des produits bien notés
        for (Rating rating : ratings) {
            if (rating.rating >= 4) {
                Product product = Product.findById(rating.productId);
                if (product != null && product.category != null) {
                    categories.add(product.category);
                }
            }
        }
        
        return categories;
    }
    
    private void diversifyRecommendations(Map<Long, Double> scores) {
        Map<String, Integer> categoryCount = new HashMap<>();
        
        // Compter les produits par catégorie
        for (Long productId : scores.keySet()) {
            Product product = Product.findById(productId);
            if (product != null && product.category != null) {
                categoryCount.merge(product.category, 1, Integer::sum);
            }
        }
        
        // Réduire les scores des catégories surreprésentées
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            Product product = Product.findById(entry.getKey());
            if (product != null && product.category != null) {
                int count = categoryCount.getOrDefault(product.category, 0);
                if (count > 3) { // Si plus de 3 produits de la même catégorie
                    double diversifiedScore = entry.getValue() * (1.0 - (count - 3) * 0.05);
                    scores.put(entry.getKey(), diversifiedScore);
                }
            }
        }
    }
    
    private RecommendedProduct createRecommendedProduct(Long productId, Double score) {
        Product product = Product.findById(productId);
        RecommendedProduct recommended = new RecommendedProduct();
        recommended.setProductId(productId);
        recommended.setProductName(product != null ? product.name : "Produit " + productId);
        recommended.setScore(score);
        recommended.setReason("Recommandé par notre système hybride intelligent");
        return recommended;
    }
}