package com.ecommerce.feedback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Feedback Service");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("documentation", "/swagger-ui.html");
        response.put("endpoints", Map.of(
            "GET /api/ratings/product/{productId}", "Liste des notes d'un produit (public)",
            "GET /api/ratings/product/{productId}/summary", "Résumé des notes (public)",
            "POST /api/ratings", "Créer/mettre à jour une note (JWT requis)",
            "DELETE /api/ratings/{ratingId}", "Supprimer une note (JWT requis)"
        ));
        return ResponseEntity.ok(response);
    }
}

