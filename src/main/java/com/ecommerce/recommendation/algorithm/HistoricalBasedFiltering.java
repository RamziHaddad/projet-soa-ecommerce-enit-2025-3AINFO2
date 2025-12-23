package com.ecommerce.recommendation.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ecommerce.recommendation.dto.RecommendedProduct;
import com.ecommerce.recommendation.entity.HistoricalRecommendation;
import com.ecommerce.recommendation.entity.Order;
import com.ecommerce.recommendation.entity.Product;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HistoricalBasedFiltering implements RecommendationAlgorithm {

    public String getAlgorithmName() {
        return "historical-based";
    }

    // Compatibilité: certaines versions s'attendent à une méthode getName()
    public String getName() {
        return getAlgorithmName();
    }

    @Override
    public List<RecommendedProduct> generateRecommendations(Long userId, Integer maxResults) {
        System.out.println("Génération de recommandations basées sur l'historique pour: " + userId);

        // Récupérer l'historique des recommandations pour cet utilisateur
        List<HistoricalRecommendation> history = HistoricalRecommendation.list("userId", userId);

        // Compter fréquences de catégories et produits
        Map<String, Integer> categoryFreq = new HashMap<>();
        Map<Long, Integer> productFreq = new HashMap<>();

        for (HistoricalRecommendation h : history) {
            if (h.categoryRecommended != null) {
                categoryFreq.put(h.categoryRecommended, categoryFreq.getOrDefault(h.categoryRecommended, 0) + 1);
            }
            if (h.productRecommendedId != null) {
                productFreq.put(h.productRecommendedId, productFreq.getOrDefault(h.productRecommendedId, 0) + 1);
            }
        }

        // Compléter avec les produits commandés récemment
        List<Order> userOrders = Order.list("userId", userId);
        for (Order o : userOrders) {
            o.items.stream().forEach(
                    item -> productFreq.put(item.getProductId(), productFreq.getOrDefault(item.getProductId(), 0) + 1));
        }

        // Construire scores pour tous les produits
        Map<Long, Double> productScores = new HashMap<>();
        List<Product> allProducts = Product.listAll();

        // Déterminer le score maximal par catégorie
        for (Product p : allProducts) {
            double score = 0.0;
            if (p.category != null && categoryFreq.containsKey(p.category)) {
                score += categoryFreq.get(p.category) * 1.0; // pondération simple
            }
            if (productFreq.containsKey(p.id)) {
                score += productFreq.get(p.id) * 2.0; // les produits déjà présents dans l'historique ont plus
                                                      // d'importance
            }
            if (score > 0) {
                productScores.put(p.id, score);
            }
        }

        // Trier et retourner
        return productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> createRecommendedProduct(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private RecommendedProduct createRecommendedProduct(Long productId, Double score) {
        Product product = Product.findById(productId);
        RecommendedProduct recommended = new RecommendedProduct();
        recommended.setProductId(productId);
        recommended.setProductName(product != null ? product.name : "Produit " + productId);
        recommended.setScore(score);
        recommended.setReason("Basé sur vos recommandations passées et votre activité");
        return recommended;
    }
}
