package ecommerce.pricing.strategy;

import ecommerce.pricing.service.FidelityService;
import ecommerce.pricing.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stratégie VIP - Prix premium avec bonus supplémentaire
 * Combine promotions + fidélité + bonus VIP de 5%
 */
@Component
public class VIPPricingStrategy implements PricingStrategy {
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private FidelityService fidelityService;
    
    private static final BigDecimal VIP_BONUS = BigDecimal.valueOf(0.05); // 5%
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingContext context) {
        // Étape 1: Appliquer les promotions actives
        BigDecimal priceAfterPromotions = promotionService.applyPromotions(
            basePrice, 
            context.getProductId()
        );
        
        // Étape 2: Appliquer la réduction fidélité
        BigDecimal priceAfterFidelity = fidelityService.applyFidelityDiscount(
            priceAfterPromotions, 
            context.getUserId()
        );
        
        // Étape 3: Appliquer le bonus VIP supplémentaire de 5%
        BigDecimal vipDiscount = priceAfterFidelity.multiply(VIP_BONUS);
        BigDecimal finalPrice = priceAfterFidelity.subtract(vipDiscount);
        
        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getStrategyName() {
        return "VIP";
    }
    
    @Override
    public String getDescription() {
        return "Prix VIP avec bonus supplémentaire de 5% (promotions + fidélité + bonus VIP)";
    }
}