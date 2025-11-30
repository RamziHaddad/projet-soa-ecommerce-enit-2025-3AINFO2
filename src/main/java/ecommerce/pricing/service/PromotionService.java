package ecommerce.pricing.service;

import ecommerce.pricing.dto.PromotionRequest;
import ecommerce.pricing.dto.PromotionResponse;
import ecommerce.pricing.entity.Promotion;
import ecommerce.pricing.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    // Créer une promotion
    public Promotion createPromotion(PromotionRequest request) {
        // Validation des dates
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
    
    // Appliquer les promotions à un prix (utilisé par PriceService)
    public BigDecimal applyPromotions(BigDecimal basePrice, Long productId) {
        // Trouver la promotion active du produit
        Optional<Promotion> activePromotion = promotionRepository
            .findActivePromotionByProductId(productId, LocalDate.now());
        
        if (activePromotion.isPresent()) {
            Promotion promotion = activePromotion.get();
            // Calculer le prix après réduction
            BigDecimal discountAmount = basePrice.multiply(
                promotion.getDiscountPercentage().divide(BigDecimal.valueOf(100))
            );
            return basePrice.subtract(discountAmount);
        }
        
        // Pas de promotion active, retourner le prix de base
        return basePrice;
    }

    // Obtenir une promotion par ID
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion non trouvée avec l'id: " + id));
        return mapToResponse(promotion);
    }

    // Obtenir la promotion active d'un produit
    public PromotionResponse getActivePromotionForProduct(Long productId) {
        Optional<Promotion> promotion = promotionRepository
            .findActivePromotionByProductId(productId, LocalDate.now());
        return promotion.map(this::mapToResponse).orElse(null);
    }

    // Obtenir toutes les promotions
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // Obtenir toutes les promotions actives
    public List<PromotionResponse> getAllActivePromotions() {
        return promotionRepository.findAllActivePromotions(LocalDate.now()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // Mettre à jour une promotion
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

    // Supprimer une promotion
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion non trouvée avec l'id: " + id);
        }
        promotionRepository.deleteById(id);
    }

    // Méthode utilitaire pour convertir Entity en Response
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
}