package ecommerce.pricing.controller;

import ecommerce.pricing.dto.PriceRequest;
import ecommerce.pricing.dto.PriceResponse;
import ecommerce.pricing.dto.BatchPriceRequest;
import ecommerce.pricing.dto.OrderValidationRequest;
import ecommerce.pricing.dto.OrderValidationResponse;
import ecommerce.pricing.dto.PriceChangeEvent;
import ecommerce.pricing.entity.Price;
import ecommerce.pricing.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    @Autowired
    private PriceService priceService;

    @PostMapping
    public ResponseEntity<Price> createPrice(@RequestBody PriceRequest request) {
        Price price = priceService.createPrice(request);
        return ResponseEntity.ok(price);
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<PriceResponse> getCurrentPrice(@PathVariable Long productId) {
        PriceResponse price = priceService.getCurrentPrice(productId);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/final")
    public ResponseEntity<PriceResponse> getFinalPrice(
            @RequestParam Long productId,
            @RequestParam(required = false) Long userId) {
        PriceResponse price = priceService.calculateFinalPrice(productId, userId);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/products")
    public ResponseEntity<List<Price>> getPricesForProducts(@RequestParam List<Long> productIds) {
        List<Price> prices = priceService.getPricesForProducts(productIds);
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/history/{productId}")
    public ResponseEntity<List<Price>> getPriceHistory(@PathVariable Long productId) {
        List<Price> priceHistory = priceService.getPriceHistory(productId);
        return ResponseEntity.ok(priceHistory);
    }

    @PutMapping("/{priceId}")
    public ResponseEntity<Price> updatePrice(@PathVariable Long priceId, @RequestBody PriceRequest request) {
        Price price = priceService.updatePrice(priceId, request);
        return ResponseEntity.ok(price);
    }

    @DeleteMapping("/{priceId}")
    public ResponseEntity<Void> deactivatePrice(@PathVariable Long priceId) {
        priceService.deactivatePrice(priceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists/{productId}")
    public ResponseEntity<Boolean> productHasPrice(@PathVariable Long productId) {
        boolean hasPrice = priceService.productHasPrice(productId);
        return ResponseEntity.ok(hasPrice);
    }
    @PostMapping("/batch")
    public ResponseEntity<List<PriceResponse>> getBatchPrices(@RequestBody BatchPriceRequest request) {
        List<PriceResponse> prices = priceService.calculateBatchPrices(request);
        return ResponseEntity.ok(prices);
    }
    @PostMapping("/validate")
    public ResponseEntity<OrderValidationResponse> validateOrderPrices(
            @RequestBody OrderValidationRequest request) {
        OrderValidationResponse response = priceService.validateOrderPrices(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/notify")
    public ResponseEntity<PriceChangeEvent> notifyPriceChange(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        PriceChangeEvent event = priceService.notifyPriceChange(id, reason);
        return ResponseEntity.ok(event);
    }
    @GetMapping("/events/price-changes")
    public ResponseEntity<List<PriceChangeEvent>> getPriceChangeEvents(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        List<PriceChangeEvent> events = priceService.getPriceChangeEvents(productId, days);
        return ResponseEntity.ok(events);
    }


}