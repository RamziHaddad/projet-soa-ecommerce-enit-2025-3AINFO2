package ecommerce.pricing.controller;

import ecommerce.pricing.dto.PriceResponse;
import ecommerce.pricing.dto.StrategyPriceRequest;
import ecommerce.pricing.service.StrategyPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour gérer le calcul de prix avec stratégies
 * Expose les endpoints pour utiliser les différentes stratégies de pricing
 */
@RestController
@RequestMapping("/api/strategy-prices")
public class StrategyPriceController {
    
    @Autowired
    private StrategyPriceService strategyPriceService;
    
    /**
     * Calculer le prix avec une stratégie spécifique
     * POST /api/strategy-prices/calculate
     * 
     * Body exemple:
     * {
     *   "productId": 1,
     *   "userId": 100,
     *   "quantity": 50,
     *   "strategyName": "WHOLESALE",
     *   "customerType": "WHOLESALE",
     *   "seasonalPeriod": "NORMAL"
     * }
     * 
     * @param request Requête avec tous les paramètres
     * @return Prix calculé selon la stratégie
     */
    @PostMapping("/calculate")
    public ResponseEntity<PriceResponse> calculateWithStrategy(
            @RequestBody StrategyPriceRequest request) {
        PriceResponse response = strategyPriceService.calculatePriceWithStrategy(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Comparer les prix avec toutes les stratégies disponibles
     * GET /api/strategy-prices/compare
     * 
     * Params:
     * - productId: ID du produit
     * - userId: ID de l'utilisateur
     * - quantity: Quantité (optionnel, défaut: 1)
     * 
     * @return Comparaison de toutes les stratégies
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareStrategies(
            @RequestParam Long productId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        
        Map<String, Object> comparison = strategyPriceService.comparePricingStrategies(
            productId, userId, quantity
        );
        
        return ResponseEntity.ok(comparison);
    }
    
    /**
     * Lister toutes les stratégies disponibles
     * GET /api/strategy-prices/strategies
     * 
     * @return Map avec nom et description de chaque stratégie
     */
    @GetMapping("/strategies")
    public ResponseEntity<Map<String, String>> listStrategies() {
        Map<String, String> strategies = strategyPriceService.getAvailableStrategies();
        return ResponseEntity.ok(strategies);
    }
    
    /**
     * Calculer le prix VIP (raccourci)
     * GET /api/strategy-prices/vip
     * 
     * @param productId ID du produit
     * @param userId ID de l'utilisateur
     * @return Prix VIP calculé
     */
    @GetMapping("/vip")
    public ResponseEntity<PriceResponse> calculateVIPPrice(
            @RequestParam Long productId,
            @RequestParam Long userId) {
        
        StrategyPriceRequest request = new StrategyPriceRequest();
        request.setProductId(productId);
        request.setUserId(userId);
        request.setQuantity(1);
        request.setStrategyName("VIP");
        request.setCustomerType("VIP");
        
        PriceResponse response = strategyPriceService.calculatePriceWithStrategy(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculer le prix en gros (raccourci)
     * GET /api/strategy-prices/wholesale
     * 
     * @param productId ID du produit
     * @param userId ID de l'utilisateur
     * @param quantity Quantité commandée
     * @return Prix wholesale calculé
     */
    @GetMapping("/wholesale")
    public ResponseEntity<PriceResponse> calculateWholesalePrice(
            @RequestParam Long productId,
            @RequestParam Long userId,
            @RequestParam Integer quantity) {
        
        StrategyPriceRequest request = new StrategyPriceRequest();
        request.setProductId(productId);
        request.setUserId(userId);
        request.setQuantity(quantity);
        request.setStrategyName("WHOLESALE");
        request.setCustomerType("WHOLESALE");
        
        PriceResponse response = strategyPriceService.calculatePriceWithStrategy(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculer le prix saisonnier (raccourci)
     * GET /api/strategy-prices/seasonal
     * 
     * @param productId ID du produit
     * @param userId ID de l'utilisateur
     * @param period Période saisonnière (BLACK_FRIDAY, CHRISTMAS, SUMMER_SALE)
     * @return Prix saisonnier calculé
     */
    @GetMapping("/seasonal")
    public ResponseEntity<PriceResponse> calculateSeasonalPrice(
            @RequestParam Long productId,
            @RequestParam Long userId,
            @RequestParam String period) {
        
        StrategyPriceRequest request = new StrategyPriceRequest();
        request.setProductId(productId);
        request.setUserId(userId);
        request.setQuantity(1);
        request.setStrategyName("SEASONAL");
        request.setSeasonalPeriod(period);
        
        PriceResponse response = strategyPriceService.calculatePriceWithStrategy(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculer le prix standard (raccourci)
     * GET /api/strategy-prices/standard
     * 
     * @param productId ID du produit
     * @param userId ID de l'utilisateur
     * @return Prix standard calculé
     */
    @GetMapping("/standard")
    public ResponseEntity<PriceResponse> calculateStandardPrice(
            @RequestParam Long productId,
            @RequestParam Long userId) {
        
        StrategyPriceRequest request = new StrategyPriceRequest();
        request.setProductId(productId);
        request.setUserId(userId);
        request.setQuantity(1);
        request.setStrategyName("STANDARD");
        
        PriceResponse response = strategyPriceService.calculatePriceWithStrategy(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculer le prix de base (raccourci)
     * GET /api/strategy-prices/base
     * 
     * @param productId ID du produit
     * @return Prix de base sans réductions
     */
    @GetMapping("/base")
    public ResponseEntity<PriceResponse> calculateBasePrice(
            @RequestParam Long productId) {
        
        StrategyPriceRequest request = new StrategyPriceRequest();
        request.setProductId(productId);
        request.setUserId(null);
        request.setQuantity(1);
        request.setStrategyName("BASE");
        
        PriceResponse response = strategyPriceService.calculatePriceWithStrategy(request);
        return ResponseEntity.ok(response);
    }
}