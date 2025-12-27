package com.ecommerce.payment.dto;

import com.ecommerce.payment.enums.TransactionStatus;
import java.time.LocalDateTime;

public class PaymentResponse {
    
    private Long transactionId;
    private Long orderId;
    private TransactionStatus status;
    private String message;
    private Double amount;
    private String currency;
    private LocalDateTime timestamp;
    
    public PaymentResponse() {}

    public PaymentResponse(Long transactionId, TransactionStatus status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public PaymentResponse(Long transactionId, Long orderId, TransactionStatus status, String message, Double amount, String currency, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}