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

@ApplicationScoped
public class CollaborativeFiltering implements RecommendationAlgorithm {
    
    @Override
    public String getName() {
        return "collaborative-filtering";
    }
    
    @Override
    public List<RecommendedProduct> generateRecommendations(Long userId, Integer maxResults) {
        System.out.println("Génération de recommandations pour l'utilisateur: " + userId);
        
        // 1. Récupérer les commandes de l'utilisateur
        List<Order> userOrders = Order.list("userId", userId);
        Set<Long> userProductIds = getUserProductIds(userOrders);
        
        System.out.println("Produits achetés par l'utilisateur: " + userProductIds);
        
        // 2. Trouver les utilisateurs similaires
        List<Long> similarUsers = findSimilarUsers(userId, userProductIds);
        System.out.println("Utilisateurs similaires: " + similarUsers);
        
        // 3. Récupérer les produits bien notés par les utilisateurs similaires
        Map<Long, Double> productScores = calculateProductScores(similarUsers, userProductIds);
        System.out.println("Scores des produits: " + productScores);
        
        // 4. Trier et retourner les recommandations
        return productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> createRecommendedProduct(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
    
    private Set<Long> getUserProductIds(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.items.stream())
                .map(item -> item.getProductId()) // Utiliser le getter
                .collect(Collectors.toSet());
    }
    
    private List<Long> findSimilarUsers(Long userId, Set<Long> userProductIds) {
        // Trouver les utilisateurs qui ont acheté au moins un produit en commun
        List<Order> allOrders = Order.listAll();
        
        Map<Long, Set<Long>> userProducts = new HashMap<>();
        for (Order order : allOrders) {
            if (!order.userId.equals(userId)) {
                Set<Long> products = userProducts.getOrDefault(order.userId, new HashSet<>());
                products.addAll(getOrderProductIds(order));
                userProducts.put(order.userId, products);
            }
        }
        
        return userProducts.entrySet().stream()
                .filter(entry -> {
                    Set<Long> commonProducts = new HashSet<>(entry.getValue());
                    commonProducts.retainAll(userProductIds);
                    return commonProducts.size() >= 1; // Au moins 1 produit en commun
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    private Set<Long> getOrderProductIds(Order order) {
        return order.items.stream()
                .map(item -> item.getProductId()) // Utiliser le getter
                .collect(Collectors.toSet());
    }
    
    private Map<Long, Double> calculateProductScores(List<Long> similarUsers, Set<Long> userProductIds) {
        Map<Long, Double> scores = new HashMap<>();
        
        for (Long similarUserId : similarUsers) {
            List<Rating> ratings = Rating.list("userId", similarUserId);
            
            for (Rating rating : ratings) {
                Long productId = rating.productId;
                if (!userProductIds.contains(productId) && rating.rating >= 4) {
                    scores.put(productId, scores.getOrDefault(productId, 0.0) + rating.rating);
                }
            }
        }
        
        return scores;
    }
    
    private RecommendedProduct createRecommendedProduct(Long productId, Double score) {
        Product product = Product.findById(productId);
        RecommendedProduct recommended = new RecommendedProduct();
        recommended.setProductId(productId);
        recommended.setProductName(product != null ? product.name : "Produit " + productId);
        recommended.setScore(score);
        recommended.setReason("Recommandé basé sur les achats d'utilisateurs similaires");
        return recommended;
    }
}