package com.example.stockservice.infrastructure;

import com.example.stockservice.application.StockService;
import com.example.stockservice.domain.Stock;
import org.springframework.web.bind.annotation.*;

@RestController // Dit à Spring : "Cette classe écoute les requêtes HTTP (Web)"
@RequestMapping("/api/stock") // Préfixe de l'URL : http://localhost:8080/api/stock/...
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    // Endpoint pour initialiser : POST /api/stock/init?id=iphone&qty=100
    @PostMapping("/init")
    public Stock init(@RequestParam String id, @RequestParam int qty) {
        return service.createStock(id, qty);
    }

    // Endpoint de consultation : GET /api/stock/iphone/available
    @GetMapping("/{id}/available")
    public int getAvailable(@PathVariable String id) {
        return service.getAvailability(id);
    }

    // Endpoint de réservation : POST /api/stock/iphone/reserve?qty=5
    @PostMapping("/{id}/reserve")
    public String reserve(@PathVariable String id, @RequestParam int qty) {
        service.reserveStock(id, qty);
        return "Réservation OK";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable String id, @RequestParam int qty) {
        service.confirmSale(id, qty);
        return "Vente Confirmée";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable String id, @RequestParam int qty) {
        service.cancelReservation(id, qty);
        return "Annulation OK";
    }
}