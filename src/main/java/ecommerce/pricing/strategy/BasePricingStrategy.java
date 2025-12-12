package ecommerce.pricing.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stratégie BASE - Prix de base sans aucune réduction
 * Retourne simplement le prix de base du produit
 */
@Component
public class BasePricingStrategy implements PricingStrategy {
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingContext context) {
        // Aucune transformation, retourne le prix de base tel quel
        return basePrice;
    }
    
    @Override
    public String getStrategyName() {
        return "BASE";
    }
    
    @Override
    public String getDescription() {
        return "Prix de base sans réductions";
    }
}