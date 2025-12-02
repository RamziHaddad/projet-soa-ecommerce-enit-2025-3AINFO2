package ecommerce.pricing.kafka.producer;

import ecommerce.pricing.kafka.event.PriceChangeKafkaEvent;
import ecommerce.pricing.kafka.event.PromotionExpiringKafkaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.price-change}")
    private String priceChangeTopic;

    @Value("${kafka.topics.promotion-expiring}")
    private String promotionExpiringTopic;

    /**
     * Publier un événement de changement de prix
     */
    public void publishPriceChangeEvent(PriceChangeKafkaEvent event) {
        logger.info("📤 Publishing price change event for product: {}", event.getProductId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(priceChangeTopic, event.getProductId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info(" Price change event sent successfully for product: {}",
                        event.getProductId());
            } else {
                logger.error(" Failed to send price change event: {}", ex.getMessage());
            }
        });
    }

         // Publier un événement de promotion qui expire bientôt
    public void publishPromotionExpiringEvent(PromotionExpiringKafkaEvent event) {
        logger.info(" Publishing promotion expiring event for promotion: {}",
                event.getPromotionId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(promotionExpiringTopic,
                        event.getPromotionId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info(" Promotion expiring event sent successfully for promotion: {}",
                        event.getPromotionId());
            } else {
                logger.error(" Failed to send promotion expiring event: {}", ex.getMessage());
            }
        });
    }
}