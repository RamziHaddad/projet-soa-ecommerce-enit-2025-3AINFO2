package ecommerce.pricing.service;

import ecommerce.pricing.dto.*;
import ecommerce.pricing.entity.Price;
import ecommerce.pricing.kafka.event.PriceChangeKafkaEvent;
import ecommerce.pricing.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PriceService {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private FidelityService fidelityService;

    @Autowired
    private PromotionService promotionService;

    // ✅ NOUVEAU: Injection du OutboxService au lieu de KafkaProducerService
    @Autowired
    private OutboxService outboxService;

    private List<PriceChangeEvent> priceChangeEvents = new ArrayList<>();

       @CacheEvict(value = "prices", allEntries = false, key = "#request.productId")
        public Price createPrice(PriceRequest request) {
        // Désactiver l'ancien prix actif s'il existe
        Optional<Price> existingActivePrice = priceRepository.findActivePriceByProductId(request.getProductId());
        existingActivePrice.ifPresent(price -> {
            price.setStatus(Price.PriceStatus.INACTIVE);
            priceRepository.save(price);
        });

        // Créer le nouveau prix
        Price newPrice = new Price();
        newPrice.setProductId(request.getProductId());
        newPrice.setBasePrice(request.getBasePrice());
        newPrice.setCurrency(request.getCurrency() != null ? request.getCurrency() : "EUR");
        newPrice.setEffectiveDate(request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now());
        newPrice.setStatus(Price.PriceStatus.ACTIVE);

        Price savedPrice = priceRepository.save(newPrice);

        // ✅ NOUVEAU: Créer événement Outbox pour le nouveau prix
        PriceChangeKafkaEvent event = new PriceChangeKafkaEvent(
                savedPrice.getProductId(),
                BigDecimal.ZERO, // Pas d'ancien prix
                savedPrice.getBasePrice(),
                "PRICE_CREATED",
                "Nouveau prix créé"
        );

        outboxService.createOutboxEvent(
                "PRICE",
                savedPrice.getId().toString(),
                "PRICE_CHANGED",
                event
        );

        return savedPrice;
    }

    @Cacheable(value = "prices", key = "#productId")
    public PriceResponse getCurrentPrice(Long productId) {
        Price price = priceRepository.findActivePriceByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Aucun prix actif trouvé pour le produit: " + productId));

        return new PriceResponse(
                price.getProductId(),
                price.getBasePrice(),
                price.getBasePrice(), // Prix sans réductions
                price.getCurrency()
        );
    }

    @Cacheable(value = "prices", key = "'final_' + #productId + '_' + #userId")
    public PriceResponse calculateFinalPrice(Long productId, Long userId) {
        // Récupérer le prix de base
        Price price = priceRepository.findActivePriceByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Prix non trouvé pour le produit: " + productId));

        BigDecimal basePrice = price.getBasePrice();
        BigDecimal priceAfterPromotions = promotionService.applyPromotions(basePrice, productId);
        BigDecimal finalPrice = fidelityService.applyFidelityDiscount(priceAfterPromotions, userId);

        return new PriceResponse(
                productId,
                basePrice,
                finalPrice,
                price.getCurrency()
        );
    }

    public List<Price> getPricesForProducts(List<Long> productIds) {
        return priceRepository.findActivePricesByProductIds(productIds);
    }

    // ✅ MODIFIÉ: updatePrice utilise maintenant l'Outbox
    @CacheEvict(value = "prices", key = "#priceId")
    public Price updatePrice(Long priceId, PriceRequest request) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Prix non trouvé avec l'id: " + priceId));

        // Sauvegarder l'ancien prix pour l'événement
        BigDecimal oldPrice = price.getBasePrice();

        price.setBasePrice(request.getBasePrice());
        if (request.getCurrency() != null) {
            price.setCurrency(request.getCurrency());
        }
        if (request.getEffectiveDate() != null) {
            price.setEffectiveDate(request.getEffectiveDate());
        }

        Price savedPrice = priceRepository.save(price);

        // ✅ NOUVEAU: Créer événement Outbox au lieu d'envoyer directement à Kafka
        PriceChangeKafkaEvent event = new PriceChangeKafkaEvent(
                savedPrice.getProductId(),
                oldPrice,
                savedPrice.getBasePrice(),
                "PRICE_UPDATED",
                "Prix mis à jour"
        );

        outboxService.createOutboxEvent(
                "PRICE",
                savedPrice.getId().toString(),
                "PRICE_CHANGED",
                event
        );

        return savedPrice;
    }

    public void deactivatePrice(Long priceId) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Prix non trouvé avec l'id: " + priceId));

        price.setStatus(Price.PriceStatus.INACTIVE);
        priceRepository.save(price);
    }

    public List<Price> getPriceHistory(Long productId) {
        return priceRepository.findByProductIdOrderByEffectiveDateDesc(productId);
    }

    public boolean productHasPrice(Long productId) {
        return priceRepository.existsByProductId(productId);
    }

    // ========== NOUVEAUX ENDPOINTS POUR INTER-MICROSERVICES ==========

    // Calculate Final price for multiple products
    public List<PriceResponse> calculateBatchPrices(BatchPriceRequest request) {
        return request.getProductIds().stream()
                .map(productId -> {
                    try {
                        return calculateFinalPrice(productId, request.getUserId());
                    } catch (RuntimeException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Validate price for an order
    public OrderValidationResponse validateOrderPrices(OrderValidationRequest request) {
        OrderValidationResponse response = new OrderValidationResponse();
        List<OrderValidationResponse.ItemPrice> itemPrices = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean allValid = true;
        String currency = "EUR";

        for (OrderValidationRequest.OrderItem item : request.getItems()) {
            try {
                PriceResponse priceResponse = calculateFinalPrice(item.getProductId(), request.getUserId());

                BigDecimal itemTotal = priceResponse.getFinalPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                itemPrices.add(new OrderValidationResponse.ItemPrice(
                        item.getProductId(),
                        priceResponse.getFinalPrice(),
                        item.getQuantity(),
                        itemTotal
                ));

                totalAmount = totalAmount.add(itemTotal);
                currency = priceResponse.getCurrency();

            } catch (RuntimeException e) {
                allValid = false;
                response.setMessage("Prix non trouvé pour le produit: " + item.getProductId());
                break;
            }
        }

        response.setValid(allValid);
        response.setTotalAmount(totalAmount);
        response.setCurrency(currency);
        response.setItemPrices(itemPrices);

        if (allValid) {
            response.setMessage("Commande validée avec succès");
        }

        return response;
    }

    // ✅ MODIFIÉ: notifyPriceChange utilise maintenant l'Outbox
    public PriceChangeEvent notifyPriceChange(Long priceId, String reason) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Prix non trouvé avec l'id: " + priceId));

        // Récupérer l'ancien prix s'il existe
        List<Price> history = priceRepository.findByProductIdOrderByEffectiveDateDesc(price.getProductId());
        BigDecimal oldPrice = history.size() > 1 ? history.get(1).getBasePrice() : BigDecimal.ZERO;

        PriceChangeEvent event = new PriceChangeEvent(
                price.getProductId(),
                oldPrice,
                price.getBasePrice(),
                "PRICE_NOTIFICATION"
        );
        event.setEventId((long) (priceChangeEvents.size() + 1));
        event.setReason(reason != null ? reason : "Price change notification");

        priceChangeEvents.add(event);

        // ✅ NOUVEAU: Utiliser Outbox au lieu d'envoyer directement à Kafka
        PriceChangeKafkaEvent kafkaEvent = new PriceChangeKafkaEvent(
                price.getProductId(),
                oldPrice,
                price.getBasePrice(),
                "PRICE_NOTIFICATION",
                reason
        );

        outboxService.createOutboxEvent(
                "PRICE",
                price.getId().toString(),
                "PRICE_CHANGED",
                kafkaEvent
        );

        return event;
    }

    // Get price change events
    public List<PriceChangeEvent> getPriceChangeEvents(Long productId, Integer days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        return priceChangeEvents.stream()
                .filter(event -> event.getTimestamp().isAfter(cutoffDate))
                .filter(event -> productId == null || event.getProductId().equals(productId))
                .sorted(Comparator.comparing(PriceChangeEvent::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    // ✅ HEALTH CHECK: Ajouté par votre ami
    public long getActivePricesCount() {
        return priceRepository.countByStatus(Price.PriceStatus.ACTIVE);
    }

@CacheEvict(value = "prices", key = "#productId")
public void evictPriceCache(Long productId) {
    System.out.println("🗑️ Invalidation manuelle du cache pour produit: " + productId);
}

/**
 * Invalider tout le cache des prix
 * Utilisé par le CacheController
 */
@CacheEvict(value = "prices", allEntries = true)
public void evictAllPricesCache() {
    System.out.println("🗑️ Invalidation de TOUT le cache des prix");
}
}