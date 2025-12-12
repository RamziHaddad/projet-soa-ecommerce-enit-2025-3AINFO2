package com.example.stockservice.infrastructure;

import com.example.stockservice.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StockRepository extends JpaRepository<Stock, String> {
}