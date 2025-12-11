package com.example.stockservice.infrastructure;

import com.example.stockservice.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

// --- EXPLICATION ---
// JpaRepository est magique. Juste en étendant cette interface,
// Spring va créer automatiquement le code pour faire :
// save(), findById(), delete(), etc. directement sur Postgres.
public interface StockRepository extends JpaRepository<Stock, String> {
}