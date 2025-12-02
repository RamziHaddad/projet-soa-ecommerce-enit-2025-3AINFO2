package ecommerce.pricing.controller;

import ecommerce.pricing.entity.Fidelity;
import ecommerce.pricing.service.FidelityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ecommerce.pricing.dto.BulkFidelityUpdateRequest;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/fidelity")
public class FidelityController {

    @Autowired
    private FidelityService fidelityService;

    // Créer ou mettre à jour la fidélité d'un utilisateur
    @PostMapping("/user/{userId}")
    public ResponseEntity<Fidelity> createOrUpdateFidelity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer points) {
        Fidelity fidelity = fidelityService.createOrUpdateFidelity(userId, points);
        return ResponseEntity.ok(fidelity);
    }

    // Obtenir tous les programmes de fidélité
    @GetMapping
    public List<Fidelity> getAllFidelities() {
        return fidelityService.getAllFidelities();
    }

    // Obtenir la fidélité par ID
    @GetMapping("/{id}")
    public ResponseEntity<Fidelity> getFidelityById(@PathVariable Long id) {
        try {
            Fidelity fidelity = fidelityService.getFidelityByUserId(id);
            return ResponseEntity.ok(fidelity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtenir la fidélité d'un utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<Fidelity> getFidelityByUserId(@PathVariable Long userId) {
        try {
            Fidelity fidelity = fidelityService.getFidelityByUserId(userId);
            return ResponseEntity.ok(fidelity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Mettre à jour la fidélité
    @PutMapping("/{id}")
    public ResponseEntity<Fidelity> updateFidelity(@PathVariable Long id, @RequestBody Fidelity fidelityDetails) {
        try {
            Fidelity updatedFidelity = fidelityService.updateFidelity(id, fidelityDetails);
            return ResponseEntity.ok(updatedFidelity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Supprimer la fidélité
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFidelity(@PathVariable Long id) {
        fidelityService.deleteFidelity(id);
        return ResponseEntity.noContent().build();
    }

    // Appliquer la réduction fidélité à un prix
    @GetMapping("/apply-discount")
    public ResponseEntity<Double> applyFidelityDiscount(
            @RequestParam Double price,
            @RequestParam Long userId) {
        Double finalPrice = fidelityService.applyFidelityDiscount(price, userId);
        return ResponseEntity.ok(finalPrice);
    }
    @PostMapping("/bulk-update")
    public ResponseEntity<Map<String, Object>> bulkUpdateFidelity(
            @RequestBody BulkFidelityUpdateRequest request) {
        Map<String, Object> result = fidelityService.bulkUpdateFidelity(request);
        return ResponseEntity.ok(result);
    }
}