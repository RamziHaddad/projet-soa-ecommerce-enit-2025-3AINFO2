package com.ecommerce.feedback.service;

import com.ecommerce.feedback.dto.CreateRatingRequest;
import com.ecommerce.feedback.dto.ProductRatingSummaryResponse;
import com.ecommerce.feedback.dto.RatingResponse;

import java.util.List;

public interface RatingService {
    
    /**
     * Crée ou met à jour une note pour un produit et un utilisateur
     * Si une note existe déjà pour ce couple (productId, userId), elle est mise à jour
     */
    RatingResponse createOrUpdateRating(Long userId, CreateRatingRequest request);
    
    /**
     * Récupère toutes les notes d'un produit
     */
    List<RatingResponse> getRatingsByProductId(Long productId);
    
    /**
     * Calcule la moyenne et le nombre de notes pour un produit
     */
    ProductRatingSummaryResponse getProductRatingSummary(Long productId);
    
    /**
     * Supprime la note d'un utilisateur
     * Vérifie que l'utilisateur est bien le propriétaire de la note
     */
    void deleteRating(Long ratingId, Long userId);
}

