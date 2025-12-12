package ecommerce.pricing.strategy;

import java.math.BigDecimal;

/**
 * Strategy Pattern - Interface principale
 * Définit le contrat pour toutes les stratégies de calcul de prix
 * 
 * Chaque stratégie implémente sa propre logique de calcul
 */
public interface PricingStrategy {
    
    /**
     * Calcule le prix en appliquant la stratégie spécifique
     * 
     * @param basePrice Prix de base du produit
     * @param context Contexte contenant toutes les informations nécessaires
     * @return Prix calculé selon la stratégie
     */
    BigDecimal calculatePrice(BigDecimal basePrice, PricingContext context);
    
    /**
     * Retourne le nom unique de la stratégie
     * Utilisé pour identifier et sélectionner la stratégie
     * 
     * @return Nom de la stratégie (ex: "BASE", "VIP", "WHOLESALE")
     */
    String getStrategyName();
    
    /**
     * Retourne une description de ce que fait la stratégie
     * Utile pour la documentation et les APIs
     * 
     * @return Description de la stratégie
     */
    String getDescription();
}