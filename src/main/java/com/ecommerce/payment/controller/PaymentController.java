package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Transaction;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Effectuer un paiement
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("Requête de paiement reçue pour la commande: {}", request.getOrderId());
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Récupérer une transaction par son ID
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        log.info("Récupération de la transaction: {}", id);
        Transaction transaction = paymentService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Récupérer toutes les transactions d'un utilisateur
     */
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        log.info("Récupération des transactions de l'utilisateur: {}", userId);
        List<Transaction> transactions = paymentService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Récupérer toutes les transactions d'une commande
     */
    @GetMapping("/orders/{orderId}/transactions")
    public ResponseEntity<List<Transaction>> getOrderTransactions(@PathVariable Long orderId) {
        log.info("Récupération des transactions de la commande: {}", orderId);
        List<Transaction> transactions = paymentService.getTransactionsByOrderId(orderId);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Rembourser une transaction
     */
    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<PaymentResponse> refundTransaction(@PathVariable Long transactionId) {
        log.info("Demande de remboursement pour la transaction: {}", transactionId);
        PaymentResponse response = paymentService.refundTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running!");
    }
}