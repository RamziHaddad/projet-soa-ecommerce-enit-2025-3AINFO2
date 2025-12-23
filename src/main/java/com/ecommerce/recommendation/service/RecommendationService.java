package com.ecommerce.recommendation.service;

import java.util.List;

import com.ecommerce.recommendation.algorithm.CollaborativeFiltering;
import com.ecommerce.recommendation.algorithm.ContentBasedFiltering;
import com.ecommerce.recommendation.algorithm.HistoricalBasedFiltering;
import com.ecommerce.recommendation.algorithm.HybridFiltering;
import com.ecommerce.recommendation.dto.RecommendationRequest;
import com.ecommerce.recommendation.dto.RecommendationResponse;
import com.ecommerce.recommendation.dto.RecommendedProduct;
import com.ecommerce.recommendation.entity.HistoricalRecommendation;
import com.ecommerce.recommendation.entity.Order;
import com.ecommerce.recommendation.entity.Product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

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

    @Inject
    EntityManager em;

    /**
     * Génère des recommandations selon l'algorithme demandé.
     * Fallback : si aucune recommandation, renvoyer les produits les mieux notés globalement.
     */
    @Transactional
    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }

        String algorithmName = (request.getAlgorithm() != null && !request.getAlgorithm().isBlank())
                ? request.getAlgorithm().trim()
                : "hybrid-filtering";

        int maxResults = (request.getMaxResults() != null && request.getMaxResults() > 0)
                ? request.getMaxResults()
                : 10;

        List<RecommendedProduct> recommendations;

        //  Normaliser l'algorithme réellement utilisé
        switch (algorithmName) {
            case "content-based":
                recommendations = contentBasedFiltering.generateRecommendations(request.getUserId(), maxResults);
                algorithmName = "content-based";
                break;
            case "collaborative-filtering":
                recommendations = collaborativeFiltering.generateRecommendations(request.getUserId(), maxResults);
                algorithmName = "collaborative-filtering";
                break;
            case "historical-based":
                recommendations = historicalBasedFiltering.generateRecommendations(request.getUserId(), maxResults);
                algorithmName = "historical-based";
                break;
            case "hybrid-filtering":
            default:
                recommendations = hybridFiltering.generateRecommendations(request.getUserId(), maxResults);
                algorithmName = "hybrid-filtering"; // algo inconnu => fallback hybrid
                break;
        }

        // Fallback Option A : top produits par moyenne des notes (cold start)
        if (recommendations == null || recommendations.isEmpty()) {
            recommendations = getTopRatedProducts(maxResults);

            // Si pas de ratings du tout, on peut fallback sur les produits "récents" (optionnel)
            if (recommendations.isEmpty()) {
                recommendations = getRecentProductsFallback(maxResults);
                algorithmName = "recent-products-fallback";
            } else {
                algorithmName = "top-rated-fallback";
            }
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setUserId(request.getUserId());
        response.setAlgorithm(algorithmName);
        response.setRecommendations(recommendations);

        // ✅ Sauvegarder l'historique sans bloquer l'API si ça échoue
        saveTopRecommendationToHistory(request.getUserId(), recommendations);

        return response;
    }

    /**
     * Top produits globalement les mieux notés (moyenne).
     
     */
    private List<RecommendedProduct> getTopRatedProducts(int maxResults) {
        List<Object[]> rows = em.createQuery(
                        "select r.productId, avg(r.rating) " +
                        "from Rating r " +
                        "group by r.productId " +
                        "order by avg(r.rating) desc",
                        Object[].class
                )
                .setMaxResults(maxResults)
                .getResultList();

        return rows.stream().map(row -> {
            Number pid = (Number) row[0];
            Long productId = pid.longValue();

            Number avgNum = (Number) row[1];
            Double avg = avgNum != null ? avgNum.doubleValue() : 0.0;

            Product p = Product.findById(productId);

            RecommendedProduct rp = new RecommendedProduct();
            rp.setProductId(productId);
            rp.setProductName(p != null ? p.name : "Produit " + productId);
            rp.setScore(avg);
            rp.setReason("Top produits (meilleures notes globales)");
            return rp;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Fallback optionnel si la table ratings est vide :
     * renvoyer les produits les plus récents (createdAt desc).
     */
    private List<RecommendedProduct> getRecentProductsFallback(int maxResults) {
        List<Product> products = Product.find("order by createdAt desc").page(0, maxResults).list();

        return products.stream().map(p -> {
            RecommendedProduct rp = new RecommendedProduct();
            rp.setProductId(p.id);
            rp.setProductName(p.name != null ? p.name : "Produit " + p.id);
            rp.setScore(1.0); // score neutre
            rp.setReason("Produits récents (fallback)");
            return rp;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Persister une ligne historique (top reco) sans casser l'API.
     */
    private void saveTopRecommendationToHistory(Long userId, List<RecommendedProduct> recommendations) {
        try {
            if (userId == null || recommendations == null || recommendations.isEmpty()) return;

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

            HistoricalRecommendation hist = new HistoricalRecommendation(userId, category, topProductId);
            hist.persist();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement historique: " + e.getMessage());
        }
    }

    public RecommendationResponse generateRecommendationsFromHistory(Long userId, Integer months, Integer maxResults) {
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
            o.items.forEach(item -> productFreq.put(item.getProductId(),
                    productFreq.getOrDefault(item.getProductId(), 0) + 1));
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

        int limit = (maxResults != null && maxResults > 0) ? maxResults : 10;

        java.util.List<RecommendedProduct> recommendations = productScores.entrySet().stream()
                .sorted(java.util.Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
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
        return List.of(
                collaborativeFiltering.getName(),
                contentBasedFiltering.getName(),
                hybridFiltering.getName(),
                historicalBasedFiltering.getName()
        );
    }
}
