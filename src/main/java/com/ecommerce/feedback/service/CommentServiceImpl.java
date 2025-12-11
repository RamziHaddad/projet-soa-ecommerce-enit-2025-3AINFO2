package com.ecommerce.feedback.service;

import com.ecommerce.feedback.client.CatalogServiceClient;
import com.ecommerce.feedback.dto.CommentResponse;
import com.ecommerce.feedback.dto.CreateCommentRequest;
import com.ecommerce.feedback.dto.UpdateCommentRequest;
import com.ecommerce.feedback.exception.ProductNotFoundException;
import com.ecommerce.feedback.exception.ResourceNotFoundException;
import com.ecommerce.feedback.exception.UnauthorizedException;
import com.ecommerce.feedback.model.Comment;
import com.ecommerce.feedback.repository.CommentRepository;
import com.ecommerce.feedback.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final RatingRepository ratingRepository;

    @Override
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {
        log.debug("Création d'un commentaire pour userId={}, productId={}", userId, request.getProductId());

        // Vérifier que le produit existe dans le service Catalog
        if (!catalogServiceClient.productExists(request.getProductId())) {
            log.warn("Tentative de création d'un commentaire pour un produit inexistant: productId={}",
                    request.getProductId());
            throw new ProductNotFoundException(request.getProductId());
        }

        // Si un ratingId est fourni, vérifier qu'il existe et appartient à
        // l'utilisateur
        if (request.getRatingId() != null) {
            ratingRepository.findById(request.getRatingId())
                    .filter(rating -> rating.getUserId().equals(userId))
                    .orElseThrow(() -> {
                        log.warn("Rating {} non trouvé ou n'appartient pas à l'utilisateur {}",
                                request.getRatingId(), userId);
                        return new ResourceNotFoundException("Rating", request.getRatingId());
                    });
        }

        // Créer le commentaire
        Comment comment = Comment.builder()
                .productId(request.getProductId())
                .userId(userId)
                .content(request.getContent())
                .ratingId(request.getRatingId())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Commentaire créé avec succès: id={}, productId={}, userId={}",
                savedComment.getId(), savedComment.getProductId(), savedComment.getUserId());

        return mapToResponse(savedComment);
    }

    @Override
    public CommentResponse updateComment(Long commentId, Long userId, UpdateCommentRequest request) {
        log.debug("Mise à jour du commentaire id={} par userId={}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // Vérifier que l'utilisateur est bien le propriétaire du commentaire
        if (!comment.getUserId().equals(userId)) {
            log.warn(
                    "Tentative de modification d'un commentaire par un utilisateur non autorisé: commentId={}, userId={}, ownerId={}",
                    commentId, userId, comment.getUserId());
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce commentaire");
        }

        // Mettre à jour le contenu
        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Commentaire mis à jour avec succès: id={}", updatedComment.getId());
        return mapToResponse(updatedComment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        log.debug("Suppression du commentaire id={} par userId={}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // Vérifier que l'utilisateur est bien le propriétaire du commentaire
        if (!comment.getUserId().equals(userId)) {
            log.warn(
                    "Tentative de suppression d'un commentaire par un utilisateur non autorisé: commentId={}, userId={}, ownerId={}",
                    commentId, userId, comment.getUserId());
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer ce commentaire");
        }

        commentRepository.delete(comment);
        log.info("Commentaire supprimé avec succès: id={}", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        log.debug("Récupération du commentaire id={}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        return mapToResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByProductId(Long productId, Pageable pageable) {
        log.debug("Récupération des commentaires pour productId={}, page={}, size={}",
                productId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Comment> comments = commentRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return comments.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByUserId(Long userId, Pageable pageable) {
        log.debug("Récupération des commentaires pour userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Comment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return comments.map(this::mapToResponse);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .productId(comment.getProductId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .ratingId(comment.getRatingId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
