package com.ecommerce.payment.repository;

import com.ecommerce.payment.entity.Transaction;
import com.ecommerce.payment.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserId(Long userId);
    
    List<Transaction> findByOrderId(Long orderId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status);
    
    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
}