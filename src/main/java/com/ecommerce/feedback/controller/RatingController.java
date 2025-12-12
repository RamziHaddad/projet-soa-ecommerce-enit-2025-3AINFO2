package com.ecommerce.feedback.controller;

import com.ecommerce.feedback.dto.CreateRatingRequest;
import com.ecommerce.feedback.dto.ProductRatingSummaryResponse;
import com.ecommerce.feedback.dto.RatingResponse;
import com.ecommerce.feedback.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ratings", description = "API pour la gestion des notes de produits")
public class RatingController {
    
    private final RatingService ratingService;
    
    @PostMapping
    @Operation(summary = "Créer ou mettre à jour une note", 
               description = "Crée une nouvelle note ou met à jour la note existante pour un produit et l'utilisateur courant")
    public ResponseEntity<RatingResponse> createOrUpdateRating(
            @Valid @RequestBody CreateRatingRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Création/mise à jour d'une note pour userId={}, productId={}", userId, request.getProductId());
        
        RatingResponse response = ratingService.createOrUpdateRating(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Récupérer toutes les notes d'un produit", 
               description = "Retourne la liste de toutes les notes pour un produit donné")
    public ResponseEntity<List<RatingResponse>> getRatingsByProductId(@PathVariable Long productId) {
        log.info("Récupération des notes pour productId={}", productId);
        List<RatingResponse> ratings = ratingService.getRatingsByProductId(productId);
        return ResponseEntity.ok(ratings);
    }
    
    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Récupérer le résumé des notes d'un produit", 
               description = "Retourne la moyenne et le nombre total de notes pour un produit")
    public ResponseEntity<ProductRatingSummaryResponse> getProductRatingSummary(@PathVariable Long productId) {
        log.info("Récupération du résumé des notes pour productId={}", productId);
        ProductRatingSummaryResponse summary = ratingService.getProductRatingSummary(productId);
        return ResponseEntity.ok(summary);
    }
    
    @DeleteMapping("/{ratingId}")
    @Operation(summary = "Supprimer une note", 
               description = "Supprime une note. Seul le propriétaire de la note peut la supprimer")
    public ResponseEntity<Void> deleteRating(@PathVariable Long ratingId, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Suppression de la note id={} par userId={}", ratingId, userId);
        
        ratingService.deleteRating(ratingId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Extrait l'userId du contexte de sécurité Spring
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Authentification requise");
        }
        
        // Le principal est l'userId (défini dans JwtAuthenticationFilter)
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            return Long.parseLong((String) principal);
        } else {
            throw new IllegalStateException("Format d'userId invalide dans l'authentification");
        }
    }
}

