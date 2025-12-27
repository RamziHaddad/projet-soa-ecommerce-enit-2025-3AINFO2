package com.ecommerce.payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final TransactionRepository transactionRepository;
    private final OrderServiceCaller orderServiceCaller;

    public PaymentService(TransactionRepository transactionRepository,
                          OrderServiceCaller orderServiceCaller) {
        this.transactionRepository = transactionRepository;
        this.orderServiceCaller = orderServiceCaller;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        MDC.put("requestId", request.getRequestId()); // structured logging

        log.info("Traitement du paiement pour la commande: {}", request.getOrderId());

        // Idempotence
        if (transactionRepository.existsByRequestId(request.getRequestId())) {
            throw new InvalidPaymentException("Doublon détecté");
        }

        validatePaymentRequest(request);

        Transaction transaction = createTransaction(request);
        transaction = transactionRepository.save(transaction);

        try {
            boolean paymentSuccess = processPaymentLogic(request);

            if (!paymentSuccess) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                notifyOrderServiceWithRetry(request.getOrderId(), "CANCELLED");
                return buildPaymentResponse(transaction);
            }

            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            notifyOrderServiceWithRetry(request.getOrderId(), "CONFIRMED");

            return buildPaymentResponse(transaction);

        } catch (Exception ex) {
            log.error("Saga failed, starting compensation", ex);

            transaction.setStatus(TransactionStatus.REFUNDED);
            transactionRepository.save(transaction);

            throw new RuntimeException(
                    "Paiement remboursé suite à l'échec de confirmation de commande");
        } finally {
            MDC.clear();
        }
    }

    @Retry(name = "orderServiceRetry", fallbackMethod = "orderServiceFallback")
    private void notifyOrderServiceWithRetry(Long orderId, String status) {
        orderServiceCaller.updateOrderStatus(orderId, status);
        log.info("Order service updated for order {} with status {}", orderId, status);
    }

    private void orderServiceFallback(Long orderId, String status, Throwable t) {
        log.error("Failed to update order service for order {} with status {} after retries", orderId, status, t);
        throw new RuntimeException("Order service unavailable, please retry later.");
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
        switch (transaction.getStatus()) {
            case SUCCESS -> message = "Paiement réussi";
            case REFUNDED -> message = "Remboursement effectué";
            default -> message = "Échec du paiement";
        }
        response.setMessage(message);

        return response;
    }
}
