package ecommerce.pricing.controller;

import ecommerce.pricing.dto.PromotionRequest;
import ecommerce.pricing.dto.PromotionResponse;
import ecommerce.pricing.entity.Promotion;
import ecommerce.pricing.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @PostMapping
    public ResponseEntity<Promotion> createPromotion(@RequestBody PromotionRequest request) {
        Promotion promotion = promotionService.createPromotion(request);
        return ResponseEntity.ok(promotion);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Long id) {
        PromotionResponse response = promotionService.getPromotionById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/product/{productId}/active")
    public ResponseEntity<PromotionResponse> getActivePromotionForProduct(@PathVariable Long productId) {
        PromotionResponse response = promotionService.getActivePromotionForProduct(productId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> getAllActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getAllActivePromotions();
        return ResponseEntity.ok(promotions);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @PathVariable Long id, @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
      @GetMapping("/batch")
    public ResponseEntity<List<PromotionResponse>> getBatchPromotions(
            @RequestParam List<Long> productIds) {
        List<PromotionResponse> promotions = promotionService.getActivePromotionsForProducts(productIds);
        return ResponseEntity.ok(promotions);
    }
}