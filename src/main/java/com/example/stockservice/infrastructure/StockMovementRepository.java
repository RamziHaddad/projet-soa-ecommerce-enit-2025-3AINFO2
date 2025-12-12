package com.example.stockservice.infrastructure;

import com.example.stockservice.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Optional<StockMovement> findByOrderIdAndType(String orderId, StockMovement.MovementType type);
}