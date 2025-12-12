package com.ecommerce.feedback.controller;

import com.ecommerce.feedback.dto.CommentResponse;
import com.ecommerce.feedback.dto.CreateCommentRequest;
import com.ecommerce.feedback.dto.UpdateCommentRequest;
import com.ecommerce.feedback.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comments", description = "API pour la gestion des commentaires de produits")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Créer un commentaire", description = "Crée un nouveau commentaire pour un produit. Nécessite une authentification JWT.")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Création d'un commentaire pour userId={}, productId={}", userId, request.getProductId());

        CommentResponse response = commentService.createComment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Mettre à jour un commentaire", description = "Met à jour le contenu d'un commentaire. Seul le propriétaire peut modifier son commentaire.")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Mise à jour du commentaire id={} par userId={}", commentId, userId);

        CommentResponse response = commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Supprimer un commentaire", description = "Supprime un commentaire. Seul le propriétaire peut supprimer son commentaire.")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Suppression du commentaire id={} par userId={}", commentId, userId);

        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Récupérer un commentaire", description = "Retourne les détails d'un commentaire spécifique")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        log.info("Récupération du commentaire id={}", commentId);
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Récupérer les commentaires d'un produit", description = "Retourne la liste paginée des commentaires pour un produit donné, triés par date (plus récents en premier)")
    public ResponseEntity<Page<CommentResponse>> getCommentsByProductId(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Récupération des commentaires pour productId={}, page={}, size={}", productId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CommentResponse> comments = commentService.getCommentsByProductId(productId, pageable);

        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer les commentaires d'un utilisateur", description = "Retourne la liste paginée des commentaires d'un utilisateur, triés par date (plus récents en premier)")
    public ResponseEntity<Page<CommentResponse>> getCommentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Récupération des commentaires pour userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CommentResponse> comments = commentService.getCommentsByUserId(userId, pageable);

        return ResponseEntity.ok(comments);
    }

    /**
     * Extrait l'userId du contexte de sécurité Spring
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Authentification requise");
        }

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
