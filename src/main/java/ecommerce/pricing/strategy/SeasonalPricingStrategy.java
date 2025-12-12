package ecommerce.pricing.strategy;

import ecommerce.pricing.service.FidelityService;
import ecommerce.pricing.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stratégie SEASONAL - Prix saisonnier avec événements spéciaux
 * Applique des réductions pour Black Friday, Noël, soldes d'été, etc.
 */
@Component
public class SeasonalPricingStrategy implements PricingStrategy {
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private FidelityService fidelityService;
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, PricingContext context) {
        // Étape 1: Appliquer les promotions standards
        BigDecimal priceAfterPromotions = promotionService.applyPromotions(
            basePrice, 
            context.getProductId()
        );
        
        // Étape 2: Appliquer la réduction saisonnière
        BigDecimal seasonalDiscountPercentage = getSeasonalDiscount(context.getSeasonalPeriod());
        BigDecimal seasonalDiscountAmount = priceAfterPromotions.multiply(seasonalDiscountPercentage);
        BigDecimal priceAfterSeasonal = priceAfterPromotions.subtract(seasonalDiscountAmount);
        
        // Étape 3: Appliquer la fidélité
        BigDecimal finalPrice = fidelityService.applyFidelityDiscount(
            priceAfterSeasonal, 
            context.getUserId()
        );
        
        return finalPrice.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Retourne le pourcentage de réduction selon la période saisonnière
     * 
     * @param period Période saisonnière
     * @return Pourcentage de réduction (0.30 = 30%)
     */
    private BigDecimal getSeasonalDiscount(String period) {
        if (period == null) {
            return BigDecimal.ZERO;
        }
        
        switch (period.toUpperCase()) {
            case "BLACK_FRIDAY":
                return BigDecimal.valueOf(0.30); // 30% de réduction
            case "CHRISTMAS":
                return BigDecimal.valueOf(0.20); // 20% de réduction
            case "SUMMER_SALE":
                return BigDecimal.valueOf(0.15); // 15% de réduction
            case "EASTER":
                return BigDecimal.valueOf(0.10); // 10% de réduction
            case "BACK_TO_SCHOOL":
                return BigDecimal.valueOf(0.12); // 12% de réduction
            default:
                return BigDecimal.ZERO; // Pas de réduction saisonnière
        }
    }
    
    @Override
    public String getStrategyName() {
        return "SEASONAL";
    }
    
    @Override
    public String getDescription() {
        return "Prix saisonnier avec réductions spéciales (Black Friday: 30%, Noël: 20%, Été: 15%)";
    }
}