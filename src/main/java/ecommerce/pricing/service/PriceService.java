package ecommerce.pricing.service;

import ecommerce.pricing.dto.PriceRequest;
import ecommerce.pricing.dto.PriceResponse;
import ecommerce.pricing.entity.Price;
import ecommerce.pricing.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PriceService {
    
    @Autowired
    private PriceRepository priceRepository;
    
    @Autowired
    private FidelityService fidelityService;
    
    @Autowired  // ✅ AJOUT : Service promotion
    private PromotionService promotionService;
    
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
        
        return priceRepository.save(newPrice);
    }
    
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
    
    public PriceResponse calculateFinalPrice(Long productId, Long userId) {
        // Récupérer le prix de base
        Price price = priceRepository.findActivePriceByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Prix non trouvé pour le produit: " + productId));
        
        BigDecimal basePrice = price.getBasePrice();
        
        // ✅ CORRECTION : APPLIQUER D'ABORD LES PROMOTIONS
        BigDecimal priceAfterPromotions = promotionService.applyPromotions(basePrice, productId);
        
        // ✅ PUIS APPLIQUER LA FIDÉLITÉ
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
    
    public Price updatePrice(Long priceId, PriceRequest request) {
        Price price = priceRepository.findById(priceId)
            .orElseThrow(() -> new RuntimeException("Prix non trouvé avec l'id: " + priceId));
        
        price.setBasePrice(request.getBasePrice());
        if (request.getCurrency() != null) {
            price.setCurrency(request.getCurrency());
        }
        if (request.getEffectiveDate() != null) {
            price.setEffectiveDate(request.getEffectiveDate());
        }
        
        return priceRepository.save(price);
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
}