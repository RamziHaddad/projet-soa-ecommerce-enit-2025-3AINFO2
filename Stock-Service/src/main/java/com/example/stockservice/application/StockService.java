package com.example.stockservice.application;

import com.example.stockservice.domain.Stock;
import com.example.stockservice.infrastructure.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Dit à Spring : "C'est un composant métier, charge-le en mémoire"
public class StockService {

    private final StockRepository repository;

    // Injection de dépendance : Spring nous donne le Repository tout prêt
    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    public Stock createStock(String productId, int qty) {
        return repository.save(new Stock(productId, qty));
    }

    public int getAvailability(String productId) {
        return repository.findById(productId)
                .map(Stock::getAvailableQuantity)
                .orElse(0);
    }

    // @Transactional est CRUCIAL ici.
    // Cela signifie : "Si une ligne de cette méthode échoue, annule tout (Rollback)".
    // C'est vital pour garantir l'intégrité des données (ACID).
    @Transactional
    public void reserveStock(String productId, int quantity) {
        // 1. Charger l'objet depuis la BDD
        Stock stock = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit inconnu"));

        // 2. Appliquer la règle métier (C'est l'objet qui décide si c'est possible)
        stock.reserve(quantity);

        // 3. Sauvegarder le nouvel état
        repository.save(stock);
    }

    @Transactional
    public void confirmSale(String productId, int quantity) {
        Stock stock = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit inconnu"));
        stock.confirmSale(quantity);
        repository.save(stock);
    }

    @Transactional
    public void cancelReservation(String productId, int quantity) {
        Stock stock = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit inconnu"));
        stock.cancelReservation(quantity);
        repository.save(stock);
    }
}