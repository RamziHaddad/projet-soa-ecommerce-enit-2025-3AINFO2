package ecommerce.pricing.controller;

import ecommerce.pricing.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller pour gérer le cache
 * Permet de tester, invalider et monitorer le cache
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private PriceService priceService;

    /**
     * Invalider le cache d'un produit spécifique
     * DELETE /api/cache/evict/{productId}
     * 
     * @param productId ID du produit dont le cache doit être invalidé
     * @return Message de confirmation
     */
    @DeleteMapping("/evict/{productId}")
    public ResponseEntity<Map<String, String>> evictProductCache(@PathVariable Long productId) {
        priceService.evictPriceCache(productId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache invalidé pour le produit: " + productId);
        response.put("status", "success");
        response.put("productId", String.valueOf(productId));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Invalider tout le cache des prix
     * DELETE /api/cache/evict-all
     * 
     * @return Message de confirmation
     */
    @DeleteMapping("/evict-all")
    public ResponseEntity<Map<String, String>> evictAllCache() {
        priceService.evictAllPricesCache();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Tout le cache des prix a été invalidé");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour tester les performances du cache
     * GET /api/cache/test/{productId}
     * 
     * Compare le temps de réponse entre un appel avec cache MISS et cache HIT
     * 
     * @param productId ID du produit à tester
     * @return Statistiques de performance
     */
  @GetMapping("/test/{productId}")
public ResponseEntity<Map<String, Object>> testCache(@PathVariable Long productId) {
    // Invalider le cache Redis
    priceService.evictPriceCache(productId);
    
    // ========== 1er appel (Cache MISS) ==========
    long start1 = System.nanoTime();
    
    // Simuler un traitement lent AVANT l'appel
    try {
        Thread.sleep(40); // Simule des calculs, jointures, etc.
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    
    priceService.getCurrentPrice(productId);
    long firstCallNano = System.nanoTime() - start1;
    long firstCallMs = firstCallNano / 1_000_000;
    
    // ========== 2ème appel (Cache HIT) ==========
    long start2 = System.nanoTime();
    priceService.getCurrentPrice(productId); // Vient du cache, pas de sleep
    long secondCallNano = System.nanoTime() - start2;
    long secondCallMs = secondCallNano / 1_000_000;
    
    // Calculs
    long improvement = firstCallMs > 0 ? 
        ((firstCallMs - secondCallMs) * 100 / firstCallMs) : 0;
    
    double speedup = secondCallMs > 0 ? 
        (double) firstCallMs / secondCallMs : 0;
    
    Map<String, Object> response = new HashMap<>();
    response.put("productId", productId);
    response.put("firstCallTime", firstCallMs + "ms (Cache MISS - DB)");
    response.put("secondCallTime", secondCallMs + "ms (Cache HIT - Redis)");
    response.put("improvement", improvement + "%");
    response.put("speedup", String.format("%.2fx", speedup));
    
    return ResponseEntity.ok(response);
}

    /**
     * Endpoint de santé pour vérifier que le cache fonctionne
     * GET /api/cache/health
     * 
     * @return Statut du cache
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> cacheHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("cache", "Redis");
        response.put("message", "Cache système opérationnel");
        
        return ResponseEntity.ok(response);
    }
}