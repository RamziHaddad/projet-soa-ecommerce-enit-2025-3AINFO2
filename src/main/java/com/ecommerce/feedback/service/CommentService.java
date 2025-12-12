package com.ecommerce.feedback.service;

import com.ecommerce.feedback.dto.CommentResponse;
import com.ecommerce.feedback.dto.CreateCommentRequest;
import com.ecommerce.feedback.dto.UpdateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    /**
     * Crée un nouveau commentaire
     */
    CommentResponse createComment(Long userId, CreateCommentRequest request);

    /**
     * Met à jour un commentaire existant
     */
    CommentResponse updateComment(Long commentId, Long userId, UpdateCommentRequest request);

    /**
     * Supprime un commentaire
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * Récupère un commentaire par son ID
     */
    CommentResponse getCommentById(Long commentId);

    /**
     * Récupère tous les commentaires d'un produit avec pagination
     */
    Page<CommentResponse> getCommentsByProductId(Long productId, Pageable pageable);

    /**
     * Récupère tous les commentaires d'un utilisateur avec pagination
     */
    Page<CommentResponse> getCommentsByUserId(Long userId, Pageable pageable);
}
