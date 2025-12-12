package ecommerce.pricing.strategy;

import ecommerce.pricing.service.FidelityService;
import ecommerce.pricing.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stratégie STANDARD - Prix avec promotions et fidélité
 * Applique les promotions actives puis la réduction fidélité
 */
@Component
public class StandardPricingStrategy implements PricingStrategy {
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private FidelityService fidelityService;
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingContext context) {
        // Étape 1: Appliquer les promotions actives
        BigDecimal priceAfterPromotions = promotionService.applyPromotions(
            basePrice, 
            context.getProductId()
        );
        
        // Étape 2: Appliquer la réduction fidélité
        BigDecimal finalPrice = fidelityService.applyFidelityDiscount(
            priceAfterPromotions, 
            context.getUserId()
        );
        
        // Arrondir à 2 décimales
        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getStrategyName() {
        return "STANDARD";
    }
    
    @Override
    public String getDescription() {
        return "Prix standard avec promotions et fidélité";
    }
}