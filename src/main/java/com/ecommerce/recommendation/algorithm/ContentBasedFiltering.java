package com.ecommerce.recommendation.algorithm;

import java.util.Arrays;
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
public class ContentBasedFiltering implements RecommendationAlgorithm {
    
    @Override
    public String getName() {
        return "content-based";
    }
    
    @Override
    public List<RecommendedProduct> generateRecommendations(Long userId, Integer maxResults) {
        System.out.println("Génération de recommandations basées sur le contenu pour: " + userId);
        
        // 1. Récupérer les produits que l'utilisateur a aimés
        List<Rating> userRatings = Rating.list("userId", userId);
        Set<Long> likedProductIds = getUserLikedProducts(userRatings);
        
        if (likedProductIds.isEmpty()) {
            // Si pas de notes, utiliser les produits achetés
            List<Order> userOrders = Order.list("userId", userId);
            likedProductIds = getUserProductIds(userOrders);
        }
        
        System.out.println("Produits aimés par l'utilisateur: " + likedProductIds);
        
        // 2. Pour chaque produit aimé, trouver des produits similaires
        Map<Long, Double> productScores = new HashMap<>();
        
        for (Long likedProductId : likedProductIds) {
            Product likedProduct = Product.findById(likedProductId);
            if (likedProduct != null) {
                findSimilarProducts(likedProduct, productScores, likedProductIds);
            }
        }
        
        System.out.println("Scores des produits similaires: " + productScores);
        
        // 3. Trier et retourner les recommandations
        return productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> createRecommendedProduct(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
    
    private Set<Long> getUserLikedProducts(List<Rating> ratings) {
        return ratings.stream()
                .filter(rating -> rating.rating >= 4)
                .map(rating -> rating.productId)
                .collect(Collectors.toSet());
    }
    
    private Set<Long> getUserProductIds(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.items.stream())
                .map(item -> item.getProductId()) // Utiliser le getter
                .collect(Collectors.toSet());
    }
    
    private void findSimilarProducts(Product likedProduct, Map<Long, Double> scores, Set<Long> excludeProducts) {
        // Recherche par catégorie
        List<Product> allProducts = Product.listAll();
        
        for (Product product : allProducts) {
            if (!excludeProducts.contains(product.id)) {
                double similarity = calculateSimilarity(likedProduct, product);
                if (similarity > 0) {
                    scores.put(product.id, scores.getOrDefault(product.id, 0.0) + similarity);
                }
            }
        }
    }
    
    private double calculateSimilarity(Product product1, Product product2) {
        double score = 0.0;
        
        // Similarité par catégorie
        if (product1.category != null && product1.category.equals(product2.category)) {
            score += 0.7;
        }
        
        // Similarité par mots-clés dans la description
        if (product1.description != null && product2.description != null) {
            score += calculateTextSimilarity(product1.description, product2.description) * 0.3;
        }
        
        return score;
    }
    
    private double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;
        
        Set<String> words1 = new HashSet<>(Arrays.asList(text1.toLowerCase().split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.toLowerCase().split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private RecommendedProduct createRecommendedProduct(Long productId, Double score) {
        Product product = Product.findById(productId);
        RecommendedProduct recommended = new RecommendedProduct();
        recommended.setProductId(productId);
        recommended.setProductName(product != null ? product.name : "Produit " + productId);
        recommended.setScore(score);
        recommended.setReason("Similaire aux produits que vous avez aimés");
        return recommended;
    }
}