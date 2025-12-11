package com.ecommerce.feedback.repository;

import com.ecommerce.feedback.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Trouve tous les commentaires d'un produit avec pagination
     * Triés par date de création décroissante (plus récents en premier)
     */
    Page<Comment> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    /**
     * Trouve tous les commentaires d'un utilisateur avec pagination
     */
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Compte le nombre de commentaires pour un produit
     */
    Long countByProductId(Long productId);
    
    /**
     * Compte le nombre de commentaires d'un utilisateur
     */
    Long countByUserId(Long userId);
    
    /**
     * Trouve tous les commentaires liés à un rating spécifique
     */
    List<Comment> findByRatingId(Long ratingId);
}
