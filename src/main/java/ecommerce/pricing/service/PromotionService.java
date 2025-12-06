package ecommerce.pricing.service;

import ecommerce.pricing.dto.PromotionRequest;
import ecommerce.pricing.dto.PromotionResponse;
import ecommerce.pricing.entity.Promotion;
import ecommerce.pricing.kafka.event.PromotionExpiringKafkaEvent;
import ecommerce.pricing.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    // ✅ NOUVEAU: Injection du OutboxService au lieu de KafkaProducerService
    @Autowired
    private OutboxService outboxService;

    public Promotion createPromotion(PromotionRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }

        Promotion promotion = new Promotion();
        promotion.setProductId(request.getProductId());
        promotion.setDiscountPercentage(request.getDiscountPercentage());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setDescription(request.getDescription());

        return promotionRepository.save(promotion);
    }

    public BigDecimal applyPromotions(BigDecimal basePrice, Long productId) {
        Optional<Promotion> activePromotion = promotionRepository
                .findActivePromotionByProductId(productId, LocalDate.now());

        if (activePromotion.isPresent()) {
            Promotion promotion = activePromotion.get();
            BigDecimal discountAmount = basePrice.multiply(
                    promotion.getDiscountPercentage().divide(BigDecimal.valueOf(100))
            );
            return basePrice.subtract(discountAmount);
        }

        return basePrice;
    }

    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion non trouvée avec l'id: " + id));
        return mapToResponse(promotion);
    }

    public PromotionResponse getActivePromotionForProduct(Long productId) {
        Optional<Promotion> promotion = promotionRepository
                .findActivePromotionByProductId(productId, LocalDate.now());
        return promotion.map(this::mapToResponse).orElse(null);
    }

    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PromotionResponse> getAllActivePromotions() {
        return promotionRepository.findAllActivePromotions(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion non trouvée avec l'id: " + id));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }

        promotion.setProductId(request.getProductId());
        promotion.setDiscountPercentage(request.getDiscountPercentage());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setDescription(request.getDescription());

        Promotion updated = promotionRepository.save(promotion);
        return mapToResponse(updated);
    }

    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion non trouvée avec l'id: " + id);
        }
        promotionRepository.deleteById(id);
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        PromotionResponse response = new PromotionResponse();
        response.setId(promotion.getId());
        response.setProductId(promotion.getProductId());
        response.setDiscountPercentage(promotion.getDiscountPercentage());
        response.setStartDate(promotion.getStartDate());
        response.setEndDate(promotion.getEndDate());
        response.setDescription(promotion.getDescription());
        response.setIsActive(promotion.isActive());
        return response;
    }

    // Récupérer les promotions actives pour plusieurs produits
    public List<PromotionResponse> getActivePromotionsForProducts(List<Long> productIds) {
        LocalDate today = LocalDate.now();

        return productIds.stream()
                .map(productId -> promotionRepository.findActivePromotionByProductId(productId, today))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ MODIFIÉ: getExpiringSoonPromotions utilise maintenant l'Outbox
    public List<PromotionResponse> getExpiringSoonPromotions(Integer days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);

        // Filtrer les promotions qui expirent bientôt ET qui sont actives
        List<Promotion> expiringPromotions = promotionRepository.findAll().stream()
                .filter(promo -> promo.isActive() &&
                        promo.getEndDate().isAfter(today) &&
                        promo.getEndDate().isBefore(futureDate))
                .collect(Collectors.toList());

        // ✅ NOUVEAU: Publier les événements vers Outbox au lieu de Kafka directement
        for (Promotion promotion : expiringPromotions) {
            long daysRemaining = ChronoUnit.DAYS.between(today, promotion.getEndDate());

            PromotionExpiringKafkaEvent kafkaEvent = new PromotionExpiringKafkaEvent(
                    promotion.getId(),
                    promotion.getProductId(),
                    promotion.getDiscountPercentage(),
                    promotion.getEndDate(),
                    (int) daysRemaining,
                    promotion.getDescription()
            );

            outboxService.createOutboxEvent(
                    "PROMOTION",
                    promotion.getId().toString(),
                    "PROMOTION_EXPIRING",
                    kafkaEvent
            );
        }

        // Mapper et trier les résultats
        return expiringPromotions.stream()
                .map(this::mapToResponse)
                .sorted((p1, p2) -> p1.getEndDate().compareTo(p2.getEndDate()))
                .collect(Collectors.toList());
    }

    // ✅ NOUVEAU: Scheduler pour vérifier automatiquement les promotions expirantes
    @Scheduled(cron = "0 0 9 * * *") // Tous les jours à 9h
    public void checkExpiringPromotions() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Promotion> expiringPromotions = promotionRepository.findAll().stream()
                .filter(promo -> promo.isActive() &&
                        promo.getEndDate().isAfter(today) &&
                        promo.getEndDate().isBefore(tomorrow))
                .collect(Collectors.toList());

        for (Promotion promotion : expiringPromotions) {
            long daysRemaining = ChronoUnit.DAYS.between(today, promotion.getEndDate());

            PromotionExpiringKafkaEvent kafkaEvent = new PromotionExpiringKafkaEvent(
                    promotion.getId(),
                    promotion.getProductId(),
                    promotion.getDiscountPercentage(),
                    promotion.getEndDate(),
                    (int) daysRemaining,
                    promotion.getDescription()
            );

            outboxService.createOutboxEvent(
                    "PROMOTION",
                    promotion.getId().toString(),
                    "PROMOTION_EXPIRING",
                    kafkaEvent
            );
        }
    }
}