package ecommerce.pricing.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KafkaHealthIndicator implements HealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaHealthIndicator.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public KafkaHealthIndicator(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    public Health health() {
        try {
            // Tester la connexion Kafka (méthode non-bloquante)
            String testTopic = "health-check-topic";
            kafkaTemplate.send(testTopic, "health-check").get();
            
            logger.info("✅ Kafka health check: UP");
            return Health.up()
                .withDetail("service", "Kafka")
                .withDetail("status", "Connected")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        } catch (Exception e) {
            logger.warn("⚠️ Kafka health check: DOWN - {}", e.getMessage());
            return Health.down()
                .withDetail("service", "Kafka")
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        }
    }
}