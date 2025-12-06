package com.ecommerce.payment.dto;

import com.ecommerce.payment.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long transactionId;
    private Long orderId;
    private TransactionStatus status;
    private String message;
    private Double amount;
    private String currency;
    private LocalDateTime timestamp;
    
    public PaymentResponse(Long transactionId, TransactionStatus status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}