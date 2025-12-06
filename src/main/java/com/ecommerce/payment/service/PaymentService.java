package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Transaction;
import com.ecommerce.payment.enums.PaymentMethod;
import com.ecommerce.payment.enums.TransactionStatus;
import com.ecommerce.payment.exception.InvalidPaymentException;
import com.ecommerce.payment.exception.ResourceNotFoundException;
import com.ecommerce.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final TransactionRepository transactionRepository;
    
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Traitement du paiement pour la commande: {}", request.getOrderId());
        
        // 1. Valider la demande
        validatePaymentRequest(request);
        
        // 2. Créer la transaction
        Transaction transaction = createTransaction(request);
        
        // 3. Traiter le paiement
        boolean paymentSuccess = processPaymentLogic(request);
        
        // 4. Mettre à jour le statut
        if (paymentSuccess) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            log.info("Paiement réussi pour la transaction: {}", transaction.getId());
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            log.warn("Paiement échoué pour la commande: {}", request.getOrderId());
        }
        
        // 5. Sauvegarder
        transaction = transactionRepository.save(transaction);
        
        // 6. Retourner la réponse
        return buildPaymentResponse(transaction);
    }
    
    private void validatePaymentRequest(PaymentRequest request) {
        // Vérifier si la méthode de paiement nécessite des infos de carte
        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            if (request.getCardNumber() == null || request.getCardNumber().isEmpty()) {
                throw new InvalidPaymentException("Le numéro de carte est obligatoire pour ce mode de paiement");
            }
            if (request.getExpiryDate() == null || request.getExpiryDate().isEmpty()) {
                throw new InvalidPaymentException("La date d'expiration est obligatoire");
            }
        }
        
        // Vérifier que le montant est raisonnable
        if (request.getAmount() <= 0) {
            throw new InvalidPaymentException("Le montant doit être supérieur à 0");
        }
        
        if (request.getAmount() > 50000) {
            throw new InvalidPaymentException("Le montant dépasse la limite autorisée");
        }
    }
    
    private Transaction createTransaction(PaymentRequest request) {
        Transaction transaction = new Transaction();
        transaction.setOrderId(request.getOrderId());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(request.getDescription());
        
        // Masquer le numéro de carte
        if (request.getCardNumber() != null) {
            transaction.setCardNumber(maskCardNumber(request.getCardNumber()));
            transaction.setCardHolderName(request.getCardHolderName());
            transaction.setExpiryDate(request.getExpiryDate());
        }
        
        return transaction;
    }
    
    private boolean processPaymentLogic(PaymentRequest request) {
        // SIMULATION : Dans un vrai système, vous communiqueriez avec une banque
        
        try {
            // Simuler un délai de traitement
            Thread.sleep(1000);
            
            // Règles métier pour simulation
            // 1. Rejeter si montant > 10000 (limite de sécurité)
            if (request.getAmount() > 10000) {
                log.warn("Montant trop élevé: {}", request.getAmount());
                return false;
            }
            
            // 2. Vérifier la date d'expiration (si carte)
            if (request.getPaymentMethod() == PaymentMethod.CARD) {
                if (!isCardValid(request.getExpiryDate())) {
                    log.warn("Carte expirée");
                    return false;
                }
            }
            
            // 3. Simuler 5% de rejets aléatoires (plus réaliste)
            if (Math.random() < 0.05) {
                log.warn("Paiement rejeté aléatoirement (simulation)");
                return false;
            }
            
            // Paiement accepté
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur lors du traitement du paiement", e);
            return false;
        }
    }
    
    private boolean isCardValid(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            
            // Vérifier que la carte n'est pas expirée
            if (year < currentYear) {
                return false;
            }
            if (year == currentYear && month < currentMonth) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la validation de la date d'expiration", e);
            return false;
        }
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
    
    private PaymentResponse buildPaymentResponse(Transaction transaction) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transaction.getId());
        response.setOrderId(transaction.getOrderId());
        response.setStatus(transaction.getStatus());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setTimestamp(transaction.getTransactionDate());
        
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            response.setMessage("Paiement effectué avec succès");
        } else if (transaction.getStatus() == TransactionStatus.FAILED) {
            response.setMessage("Le paiement a échoué. Veuillez réessayer.");
        } else {
            response.setMessage("Paiement en cours de traitement");
        }
        
        return response;
    }
    
    // Méthodes supplémentaires
    
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction non trouvée avec l'ID: " + id));
    }
    
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
    
    public List<Transaction> getTransactionsByOrderId(Long orderId) {
        return transactionRepository.findByOrderId(orderId);
    }
    
    @Transactional
    public PaymentResponse refundTransaction(Long transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        
        // Vérifier que la transaction peut être remboursée
        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new InvalidPaymentException("Seules les transactions réussies peuvent être remboursées");
        }
        
        // Mettre à jour le statut
        transaction.setStatus(TransactionStatus.REFUNDED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        
        log.info("Remboursement effectué pour la transaction: {}", transactionId);
        
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transaction.getId());
        response.setOrderId(transaction.getOrderId());
        response.setStatus(TransactionStatus.REFUNDED);
        response.setMessage("Remboursement effectué avec succès");
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setTimestamp(LocalDateTime.now());
        
        return response;
    }
}