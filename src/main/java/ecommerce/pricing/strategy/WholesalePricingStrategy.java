package ecommerce.pricing.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stratégie WHOLESALE - Prix en gros avec réductions par quantité
 * Plus la quantité est élevée, plus la réduction est importante
 */
@Component
public class WholesalePricingStrategy implements PricingStrategy {
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingContext context) {
        Integer quantity = context.getQuantity();
        BigDecimal discountPercentage = calculateDiscountByQuantity(quantity);
        
        // Calculer le montant de la réduction
        BigDecimal discountAmount = basePrice.multiply(discountPercentage);
        
        // Soustraire la réduction du prix de base
        BigDecimal finalPrice = basePrice.subtract(discountAmount);
        
        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcule le pourcentage de réduction en fonction de la quantité
     * 
     * @param quantity Quantité commandée
     * @return Pourcentage de réduction (0.05 = 5%)
     */
    private BigDecimal calculateDiscountByQuantity(Integer quantity) {
        if (quantity >= 100) {
            return BigDecimal.valueOf(0.20); // 20% de réduction
        } else if (quantity >= 50) {
            return BigDecimal.valueOf(0.15); // 15% de réduction
        } else if (quantity >= 20) {
            return BigDecimal.valueOf(0.10); // 10% de réduction
        } else if (quantity >= 10) {
            return BigDecimal.valueOf(0.05); // 5% de réduction
        } else {
            return BigDecimal.ZERO; // Pas de réduction
        }
    }
    
    @Override
    public String getStrategyName() {
        return "WHOLESALE";
    }
    
    @Override
    public String getDescription() {
        return "Prix en gros avec réductions par quantité (10+ : 5%, 20+ : 10%, 50+ : 15%, 100+ : 20%)";
    }
}