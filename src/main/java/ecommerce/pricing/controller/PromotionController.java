package com.ecommerce.pricing_service.controller;
import com.ecommerce.pricing_service.dto.PromotionRequestDTO;
import com.ecommerce.pricing_service.dto.PromotionResponseDTO;
import com.ecommerce.pricing_service.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponseDTO> createPromotion(
            @Valid @RequestBody PromotionRequestDTO request) {
        PromotionResponseDTO response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponseDTO> getPromotionById(@PathVariable Long id) {
        PromotionResponseDTO response = promotionService.getPromotionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}/active")
    public ResponseEntity<PromotionResponseDTO> getActivePromotionForProduct(
            @PathVariable Long productId) {
        PromotionResponseDTO response = promotionService.getActivePromotionForProduct(productId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PromotionResponseDTO>> getAllPromotions() {
        List<PromotionResponseDTO> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponseDTO>> getAllActivePromotions() {
        List<PromotionResponseDTO> promotions = promotionService.getAllActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponseDTO> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequestDTO request) {
        PromotionResponseDTO response = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}