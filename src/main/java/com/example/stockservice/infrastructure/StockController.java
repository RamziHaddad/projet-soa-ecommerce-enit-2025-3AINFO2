package com.example.stockservice.infrastructure;

import com.example.stockservice.application.StockService;
import com.example.stockservice.domain.Stock;
import com.example.stockservice.dto.StockRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @PostMapping("/init")
    public ResponseEntity<Stock> init(@RequestParam String id, @RequestParam int qty) {
        return ResponseEntity.ok(service.initStock(id, qty));
    }

    @GetMapping("/{id}/available")
    public ResponseEntity<Integer> getAvailable(@PathVariable String id) {
        return ResponseEntity.ok(service.getAvailability(id));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<String> reserve(@PathVariable String id, @Valid @RequestBody StockRequest request) {
        service.reserveStock(id, request.orderId(), request.quantity());
        return ResponseEntity.ok("Réservé avec succès");
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirm(@PathVariable String id, @Valid @RequestBody StockRequest request) {
        service.confirmSale(id, request.orderId(), request.quantity());
        return ResponseEntity.ok("Vente confirmée");
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable String id, @Valid @RequestBody StockRequest request) {
        service.cancelReservation(id, request.orderId(), request.quantity());
        return ResponseEntity.ok("Réservation annulée");
    }

    @PostMapping("/{id}/correction")
    public ResponseEntity<String> correction(@PathVariable String id, @RequestParam int qty, @RequestParam String reason) {
        service.removeStockManually(id, qty, reason);
        return ResponseEntity.ok("Stock corrigé (diminué de " + qty + ") pour raison : " + reason);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        service.deleteProduct(id);
        return ResponseEntity.ok("Produit " + id + " supprimé définitivement de la base.");
    }
}