package ecommerce.pricing.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory Pattern pour gérer les stratégies de pricing
 * Permet de sélectionner dynamiquement la stratégie appropriée
 * 
 * Cette factory est automatiquement initialisée par Spring avec toutes
 * les implémentations de PricingStrategy disponibles
 */
@Component
public class PricingStrategyFactory {
    
    private final Map<String, PricingStrategy> strategies = new HashMap<>();
    
    /**
     * Constructeur avec injection automatique de toutes les stratégies
     * Spring injecte automatiquement toutes les classes qui implémentent PricingStrategy
     * 
     * @param strategyList Liste de toutes les stratégies disponibles
     */
    @Autowired
    public PricingStrategyFactory(List<PricingStrategy> strategyList) {
        // Enregistrer toutes les stratégies par leur nom
        for (PricingStrategy strategy : strategyList) {
            strategies.put(strategy.getStrategyName(), strategy);
        }
        
        System.out.println("✅ Factory initialisée avec " + strategies.size() + " stratégies: " + strategies.keySet());
    }
    
    /**
     * Récupérer une stratégie par son nom
     * 
     * @param strategyName Nom de la stratégie (BASE, STANDARD, WHOLESALE, VIP, SEASONAL)
     * @return La stratégie correspondante
     * @throws IllegalArgumentException Si la stratégie n'existe pas
     */
    public PricingStrategy getStrategy(String strategyName) {
        if (strategyName == null || strategyName.trim().isEmpty()) {
            return getDefaultStrategy();
        }
        
        PricingStrategy strategy = strategies.get(strategyName.toUpperCase());
        
        if (strategy == null) {
            throw new IllegalArgumentException(
                "Stratégie de pricing inconnue: " + strategyName + 
                ". Stratégies disponibles: " + strategies.keySet()
            );
        }
        
        return strategy;
    }
    
    /**
     * Récupérer la stratégie par défaut (STANDARD)
     * 
     * @return La stratégie standard
     */
    public PricingStrategy getDefaultStrategy() {
        return strategies.getOrDefault("STANDARD", strategies.values().iterator().next());
    }
    
    /**
     * Lister toutes les stratégies disponibles
     * 
     * @return Map avec le nom et la description de chaque stratégie
     */
    public Map<String, String> listAvailableStrategies() {
        Map<String, String> availableStrategies = new HashMap<>();
        
        for (PricingStrategy strategy : strategies.values()) {
            availableStrategies.put(
                strategy.getStrategyName(), 
                strategy.getDescription()
            );
        }
        
        return availableStrategies;
    }
    
    /**
     * Sélectionner automatiquement la meilleure stratégie selon le contexte
     * 
     * Logique de sélection:
     * - VIP si le client est VIP
     * - WHOLESALE si le client est un grossiste
     * - SEASONAL si on est dans une période spéciale
     * - STANDARD par défaut
     * 
     * @param context Contexte du calcul
     * @return La stratégie la plus appropriée
     */
    public PricingStrategy autoSelectStrategy(PricingContext context) {
        // Priorité 1: Client VIP
        if ("VIP".equalsIgnoreCase(context.getCustomerType())) {
            return getStrategy("VIP");
        }
        
        // Priorité 2: Client grossiste (wholesale)
        if ("WHOLESALE".equalsIgnoreCase(context.getCustomerType())) {
            return getStrategy("WHOLESALE");
        }
        
        // Priorité 3: Période saisonnière spéciale
        if (context.getSeasonalPeriod() != null && 
            !"NORMAL".equalsIgnoreCase(context.getSeasonalPeriod())) {
            return getStrategy("SEASONAL");
        }
        
        // Par défaut: stratégie standard
        return getDefaultStrategy();
    }
    
    /**
     * Vérifier si une stratégie existe
     * 
     * @param strategyName Nom de la stratégie
     * @return true si la stratégie existe
     */
    public boolean hasStrategy(String strategyName) {
        return strategies.containsKey(strategyName.toUpperCase());
    }
}