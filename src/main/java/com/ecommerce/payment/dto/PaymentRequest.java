package com.ecommerce.payment.dto;

import com.ecommerce.payment.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
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
    
    // Informations de carte (optionnel selon la méthode)
    @Pattern(regexp = "^[0-9]{16}$", message = "Le numéro de carte doit contenir 16 chiffres")
    private String cardNumber;
    
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String cardHolderName;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{4}$", message = "Format de date invalide (MM/YYYY)")
    private String expiryDate;
    
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV invalide")
    private String cvv; // Ne sera jamais stocké
    
    private String description;
}