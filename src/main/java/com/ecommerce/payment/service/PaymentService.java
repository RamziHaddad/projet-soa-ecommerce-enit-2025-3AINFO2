package com.ecommerce.payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.payment.client.OrderClient;
import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Transaction;
import com.ecommerce.payment.enums.PaymentMethod;
import com.ecommerce.payment.enums.TransactionStatus;
import com.ecommerce.payment.exception.InvalidPaymentException;
import com.ecommerce.payment.exception.ResourceNotFoundException;
import com.ecommerce.payment.repository.TransactionRepository;

@Service
public class PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final TransactionRepository transactionRepository;
    private final OrderClient orderClient;

    public PaymentService(TransactionRepository transactionRepository, OrderClient orderClient) {
        this.transactionRepository = transactionRepository;
        this.orderClient = orderClient;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Traitement du paiement pour la commande: {}", request.getOrderId());
        
        // --- 1. SÉCURITÉ : IDEMPOTENCE ---
        if (transactionRepository.existsByRequestId(request.getRequestId())) {
            log.warn("Tentative de doublon détectée pour le requestId: {}", request.getRequestId());
            throw new InvalidPaymentException("Doublon détecté : Ce paiement a déjà été traité !");
        }

        validatePaymentRequest(request);
        Transaction transaction = createTransaction(request);
        
        boolean paymentSuccess = processPaymentLogic(request);
        
        if (paymentSuccess) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            log.info("Paiement réussi pour la transaction: {}", transaction.getId());
            try {
                orderClient.updateOrderStatus(request.getOrderId(), "CONFIRMED");
            } catch (Exception e) {
                log.error("Erreur notification commande", e);
            }
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            try {
                orderClient.updateOrderStatus(request.getOrderId(), "CANCELLED");
            } catch (Exception e) {
                log.error("Erreur notification échec commande", e);
            }
        }
        
        transaction = transactionRepository.save(transaction);
        return buildPaymentResponse(transaction);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));
    }
    
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByOrderId(Long orderId) {
        return transactionRepository.findByOrderId(orderId);
    }

    public PaymentResponse refundTransaction(Long transactionId) {
        log.info("Traitement du remboursement pour la transaction: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable pour le remboursement"));

        if (transaction.getStatus() == TransactionStatus.REFUNDED) {
             throw new InvalidPaymentException("Cette transaction est déjà remboursée.");
        }

        transaction.setStatus(TransactionStatus.REFUNDED);
        transaction = transactionRepository.save(transaction);
        
        log.info("Transaction {} remboursée avec succès", transactionId);
        
        return buildPaymentResponse(transaction);
    }
    
    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            if (request.getCardNumber() == null || request.getCardNumber().isEmpty()) {
                throw new InvalidPaymentException("Numéro de carte obligatoire");
            }
            if (request.getExpiryDate() == null || request.getExpiryDate().isEmpty()) {
                throw new InvalidPaymentException("Date expiration obligatoire");
            }
        }
        if (request.getAmount() <= 0) throw new InvalidPaymentException("Le montant doit être > 0");
    }
    
    private Transaction createTransaction(PaymentRequest request) {
        Transaction transaction = new Transaction();
        transaction.setRequestId(request.getRequestId());
        
        transaction.setOrderId(request.getOrderId());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(request.getDescription());
        
        if (request.getCardNumber() != null && request.getCardNumber().length() >= 4) {
            transaction.setCardNumber("**** **** **** " + request.getCardNumber().substring(request.getCardNumber().length() - 4));
            transaction.setCardHolderName(request.getCardHolderName());
            transaction.setExpiryDate(request.getExpiryDate());
        }
        return transaction;
    }
    
    private boolean processPaymentLogic(PaymentRequest request) {
        return request.getAmount() <= 10000;
    }
    
    private PaymentResponse buildPaymentResponse(Transaction transaction) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transaction.getId());
        response.setOrderId(transaction.getOrderId());
        response.setStatus(transaction.getStatus());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setTimestamp(transaction.getTransactionDate());
        
        String message;
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            message = "Paiement réussi";
        } else if (transaction.getStatus() == TransactionStatus.REFUNDED) {
            message = "Remboursement effectué";
        } else {
            message = "Échec du paiement";
        }
        response.setMessage(message);
        
        return response;
    }
}