package ecommerce.pricing.health;

import ecommerce.pricing.service.PriceService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ServiceHealthIndicator implements HealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceHealthIndicator.class);
    private final PriceService priceService;
    
    public ServiceHealthIndicator(PriceService priceService) {
        this.priceService = priceService;
    }
    
    @Override
    public Health health() {
        try {
            // Vérifier si le service peut accéder aux données
            long activePricesCount = priceService.getActivePricesCount();
            
            Health.Builder healthBuilder = Health.up()
                .withDetail("service", "Pricing Service")
                .withDetail("activePrices", activePricesCount)
                .withDetail("timestamp", System.currentTimeMillis())
                .withDetail("version", "1.0.0");
            
            // Ajouter des avertissements si nécessaire
            if (activePricesCount == 0) {
                healthBuilder.withDetail("warning", "No active prices found");
            }
            
            logger.info("✅ Service health check: UP - {} active prices", activePricesCount);
            return healthBuilder.build();
            
        } catch (Exception e) {
            logger.error("❌ Service health check failed: {}", e.getMessage());
            return Health.down()
                .withDetail("service", "Pricing Service")
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        }
    }
}