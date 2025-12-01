package com.ecommerce.feedback.repository;

import com.ecommerce.feedback.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    /**
     * Trouve une note par productId et userId
     */
    Optional<Rating> findByProductIdAndUserId(Long productId, Long userId);
    
    /**
     * Liste toutes les notes d'un produit
     */
    List<Rating> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    /**
     * Calcule la moyenne des notes d'un produit
     */
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.productId = :productId")
    Double calculateAverageByProductId(@Param("productId") Long productId);
    
    /**
     * Compte le nombre de notes d'un produit
     */
    Long countByProductId(Long productId);
    
    /**
     * Vérifie si une note existe pour un produit et un utilisateur
     */
    boolean existsByProductIdAndUserId(Long productId, Long userId);
}

