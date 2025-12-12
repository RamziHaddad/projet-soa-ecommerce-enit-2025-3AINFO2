package ecommerce.pricing.service;

import ecommerce.pricing.dto.PriceResponse;
import ecommerce.pricing.dto.StrategyPriceRequest;
import ecommerce.pricing.entity.Price;
import ecommerce.pricing.repository.PriceRepository;
import ecommerce.pricing.strategy.PricingContext;
import ecommerce.pricing.strategy.PricingStrategy;
import ecommerce.pricing.strategy.PricingStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service orchestrateur pour le calcul de prix avec stratégies
 * Coordonne l'utilisation des stratégies de pricing
 */
@Service
public class StrategyPriceService {
    
    @Autowired
    private PriceRepository priceRepository;
    
    @Autowired
    private PricingStrategyFactory strategyFactory;
    
    /**
     * Calculer le prix avec une stratégie spécifique
     * 
     * @param request Requête contenant tous les paramètres
     * @return Réponse avec le prix calculé
     */
    public PriceResponse calculatePriceWithStrategy(StrategyPriceRequest request) {
        // 1. Récupérer le prix de base du produit
        Price price = priceRepository.findActivePriceByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException(
                    "Prix non trouvé pour le produit: " + request.getProductId()
                ));
        
        // 2. Créer le contexte de pricing
        PricingContext context = new PricingContext(
            request.getProductId(), 
            request.getUserId()
        );
        context.setQuantity(request.getQuantity());
        context.setCustomerType(request.getCustomerType());
        context.setSeasonalPeriod(request.getSeasonalPeriod());
        
        // 3. Sélectionner la stratégie appropriée
        PricingStrategy strategy;
        if (request.getStrategyName() != null && !request.getStrategyName().isEmpty()) {
            // Stratégie spécifiée explicitement
            strategy = strategyFactory.getStrategy(request.getStrategyName());
            System.out.println("📊 Utilisation de la stratégie: " + strategy.getStrategyName());
        } else {
            // Sélection automatique de la stratégie
            strategy = strategyFactory.autoSelectStrategy(context);
            System.out.println("🤖 Sélection automatique de la stratégie: " + strategy.getStrategyName());
        }
        
        // 4. Calculer le prix final avec la stratégie
        BigDecimal finalPrice = strategy.calculatePrice(price.getBasePrice(), context);
        
        // 5. Créer et retourner la réponse
        PriceResponse response = new PriceResponse(
            request.getProductId(),
            price.getBasePrice(),
            finalPrice,
            price.getCurrency()
        );
        
        System.out.println("💰 Prix calculé: " + price.getBasePrice() + " → " + finalPrice + " " + price.getCurrency());
        
        return response;
    }
    
    /**
     * Comparer les prix avec toutes les stratégies disponibles
     * Utile pour montrer au client toutes les options
     * 
     * @param productId ID du produit
     * @param userId ID de l'utilisateur
     * @param quantity Quantité
     * @return Map avec les résultats de toutes les stratégies
     */
    public Map<String, Object> comparePricingStrategies(Long productId, Long userId, Integer quantity) {
        // Récupérer le prix de base
        Price price = priceRepository.findActivePriceByProductId(productId)
                .orElseThrow(() -> new RuntimeException(
                    "Prix non trouvé pour le produit: " + productId
                ));
        
        // Créer le contexte
        PricingContext context = new PricingContext(productId, userId);
        context.setQuantity(quantity);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("productId", productId);
        comparison.put("basePrice", price.getBasePrice());
        comparison.put("currency", price.getCurrency());
        comparison.put("quantity", quantity);
        
        Map<String, BigDecimal> strategyPrices = new HashMap<>();
        
        // Calculer avec chaque stratégie disponible
        for (String strategyName : strategyFactory.listAvailableStrategies().keySet()) {
            try {
                PricingStrategy strategy = strategyFactory.getStrategy(strategyName);
                BigDecimal calculatedPrice = strategy.calculatePrice(price.getBasePrice(), context);
                strategyPrices.put(strategyName, calculatedPrice);
            } catch (Exception e) {
                System.err.println("❌ Erreur avec la stratégie " + strategyName + ": " + e.getMessage());
                strategyPrices.put(strategyName, null);
            }
        }
        
        comparison.put("strategies", strategyPrices);
        
        // Trouver la meilleure offre (prix le plus bas)
        BigDecimal bestPrice = strategyPrices.values().stream()
                .filter(p -> p != null)
                .min(BigDecimal::compareTo)
                .orElse(price.getBasePrice());
        
        comparison.put("bestPrice", bestPrice);
        
        // Trouver quelle stratégie donne le meilleur prix
        String bestStrategy = strategyPrices.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().equals(bestPrice))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("BASE");
        
        comparison.put("bestStrategy", bestStrategy);
        
        // Calculer l'économie par rapport au prix de base
        BigDecimal savings = price.getBasePrice().subtract(bestPrice);
        BigDecimal savingsPercentage = savings.divide(price.getBasePrice(), 4, BigDecimal.ROUND_HALF_UP)
                                              .multiply(BigDecimal.valueOf(100));
        
        comparison.put("maxSavings", savings);
        comparison.put("maxSavingsPercentage", savingsPercentage.setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
        
        return comparison;
    }
    
    /**
     * Lister toutes les stratégies disponibles
     * 
     * @return Map avec nom et description de chaque stratégie
     */
    public Map<String, String> getAvailableStrategies() {
        return strategyFactory.listAvailableStrategies();
    }
    
    /**
     * Vérifier si une stratégie existe
     * 
     * @param strategyName Nom de la stratégie
     * @return true si la stratégie existe
     */
    public boolean isStrategyAvailable(String strategyName) {
        return strategyFactory.hasStrategy(strategyName);
    }
}