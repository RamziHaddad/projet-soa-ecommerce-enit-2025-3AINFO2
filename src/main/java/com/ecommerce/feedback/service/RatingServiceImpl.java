package com.ecommerce.feedback.service;

import com.ecommerce.feedback.client.CatalogServiceClient;
import com.ecommerce.feedback.dto.CreateRatingRequest;
import com.ecommerce.feedback.dto.ProductRatingSummaryResponse;
import com.ecommerce.feedback.dto.RatingResponse;
import com.ecommerce.feedback.exception.ProductNotFoundException;
import com.ecommerce.feedback.exception.ResourceNotFoundException;
import com.ecommerce.feedback.exception.UnauthorizedException;
import com.ecommerce.feedback.model.Rating;
import com.ecommerce.feedback.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RatingServiceImpl implements RatingService {
    
    private final RatingRepository ratingRepository;
    private final CatalogServiceClient catalogServiceClient;
    
    @Override
    public RatingResponse createOrUpdateRating(Long userId, CreateRatingRequest request) {
        log.debug("Création ou mise à jour d'une note pour userId={}, productId={}, score={}", 
                  userId, request.getProductId(), request.getScore());
        
        // Vérifier que le produit existe dans le service Catalog
        if (!catalogServiceClient.productExists(request.getProductId())) {
            log.warn("Tentative de création d'une note pour un produit inexistant: productId={}", request.getProductId());
            throw new ProductNotFoundException(request.getProductId());
        }
        
        // Vérifier si une note existe déjà pour ce couple (productId, userId)
        Rating rating = ratingRepository.findByProductIdAndUserId(request.getProductId(), userId)
                .orElse(null);
        
        if (rating != null) {
            // Mise à jour de la note existante
            log.debug("Mise à jour de la note existante id={}", rating.getId());
            rating.setScore(request.getScore());
        } else {
            // Création d'une nouvelle note
            log.debug("Création d'une nouvelle note");
            rating = Rating.builder()
                    .productId(request.getProductId())
                    .userId(userId)
                    .score(request.getScore())
                    .build();
        }
        
        Rating savedRating = ratingRepository.save(rating);
        log.info("Note sauvegardée avec succès: id={}, productId={}, userId={}, score={}", 
                 savedRating.getId(), savedRating.getProductId(), savedRating.getUserId(), savedRating.getScore());
        
        return mapToResponse(savedRating);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByProductId(Long productId) {
        log.debug("Récupération des notes pour productId={}", productId);
        List<Rating> ratings = ratingRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return ratings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductRatingSummaryResponse getProductRatingSummary(Long productId) {
        log.debug("Calcul du résumé des notes pour productId={}", productId);
        
        Double average = ratingRepository.calculateAverageByProductId(productId);
        Long count = ratingRepository.countByProductId(productId);
        
        // Si aucune note n'existe, average sera null
        if (average == null) {
            average = 0.0;
        }
        
        return ProductRatingSummaryResponse.builder()
                .productId(productId)
                .average(average)
                .ratingsCount(count)
                .build();
    }
    
    @Override
    public void deleteRating(Long ratingId, Long userId) {
        log.debug("Suppression de la note id={} par userId={}", ratingId, userId);
        
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", ratingId));
        
        // Vérifier que l'utilisateur est bien le propriétaire de la note
        if (!rating.getUserId().equals(userId)) {
            log.warn("Tentative de suppression d'une note par un utilisateur non autorisé: ratingId={}, userId={}, ownerId={}", 
                     ratingId, userId, rating.getUserId());
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cette note");
        }
        
        ratingRepository.delete(rating);
        log.info("Note supprimée avec succès: id={}", ratingId);
    }
    
    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .productId(rating.getProductId())
                .userId(rating.getUserId())
                .score(rating.getScore())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}

