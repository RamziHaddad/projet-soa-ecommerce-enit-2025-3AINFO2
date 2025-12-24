package com.ecommerce.payment.dto;

import com.ecommerce.payment.enums.PaymentMethod;
import jakarta.validation.constraints.*;

public class PaymentRequest {
    
    private String requestId;

    @NotNull(message = "Order ID est obligatoire")
    private Long orderId;
    
    @NotNull(message = "User ID est obligatoire")
    private Long userId;
    
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Double amount;
    
    @NotBlank(message = "La devise est obligatoire")
    @Size(min = 3, max = 3, message = "La devise doit contenir 3 caractères (ex: EUR, USD)")
    private String currency;
    
    @NotNull(message = "La méthode de paiement est obligatoire")
    private PaymentMethod paymentMethod;
    
    @Pattern(regexp = "^[0-9]{16}$", message = "Le numéro de carte doit contenir 16 chiffres")
    private String cardNumber;
    
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String cardHolderName;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{4}$", message = "Format de date invalide (MM/YYYY)")
    private String expiryDate;
    
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV invalide")
    private String cvv; 
    
    private String description;

    // --- CONSTRUCTEUR VIDE ---
    public PaymentRequest() {}

    // --- GETTERS & SETTERS ---
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}