package com.example.stockservice.application;

import com.example.stockservice.domain.Stock;
import com.example.stockservice.domain.StockMovement;
import com.example.stockservice.domain.StockMovement.MovementType;
import com.example.stockservice.infrastructure.StockMovementRepository;
import com.example.stockservice.infrastructure.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepo;
    private final StockMovementRepository movementRepo;

    public StockService(StockRepository stockRepo, StockMovementRepository movementRepo) {
        this.stockRepo = stockRepo;
        this.movementRepo = movementRepo;
    }


    public Stock initStock(String productId, int qty) {
        return stockRepo.save(new Stock(productId, qty));
    }


    public int getAvailability(String productId) {
        return stockRepo.findById(productId)
                .map(Stock::getAvailableQuantity)
                .orElse(0); // Retourne 0 si le produit n'existe pas encore
    }


    @Transactional
    public void reserveStock(String productId, String orderId, int quantity) {

        if (movementRepo.findByOrderIdAndType(orderId, MovementType.RESERVE).isPresent()) {
            return;
        }


        Stock stock = stockRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit inconnu : " + productId));


        stock.reserve(quantity);


        stockRepo.save(stock);
        movementRepo.save(new StockMovement(productId, orderId, MovementType.RESERVE, quantity));
    }


    @Transactional
    public void confirmSale(String productId, String orderId, int quantity) {

        if (movementRepo.findByOrderIdAndType(orderId, MovementType.CONFIRM).isPresent()) {
            return;
        }

        Stock stock = stockRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit inconnu"));


        stock.confirmSale(quantity);


        stockRepo.save(stock);
        movementRepo.save(new StockMovement(productId, orderId, MovementType.CONFIRM, quantity));
    }


    @Transactional
    public void cancelReservation(String productId, String orderId, int quantity) {

        if (movementRepo.findByOrderIdAndType(orderId, MovementType.CANCEL).isPresent()) {
            return;
        }


        boolean hasReservation = movementRepo.findByOrderIdAndType(orderId, MovementType.RESERVE).isPresent();
        if (!hasReservation) {

            return;
        }

        Stock stock = stockRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit inconnu"));

        stock.cancelReservation(quantity);

        stockRepo.save(stock);
        movementRepo.save(new StockMovement(productId, orderId, MovementType.CANCEL, quantity));
    }

    @Transactional
    public void removeStockManually(String productId, int quantity, String reason) {
        Stock stock = stockRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit inconnu"));


        if (stock.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("Pas assez de stock pour retirer cette quantité");
        }


        stock.confirmSale(quantity);

        stockRepo.save(stock);


        movementRepo.save(new StockMovement(productId, "MANUAL_ADJUST: " + reason, MovementType.ADJUSTMENT, quantity));
    }


    @Transactional
    public void deleteProduct(String productId) {

        if (!stockRepo.existsById(productId)) {
            throw new IllegalArgumentException("Produit déjà inexistant");
        }

        Stock stock = stockRepo.findById(productId).get();
        if (stock.getQuantityTotal() > 0) {
            throw new IllegalStateException("Impossible de supprimer : Il reste " + stock.getQuantityTotal() + " unités en stock. Faites une correction d'abord.");
        }


        movementRepo.save(new StockMovement(productId, "ADMIN_DELETE", MovementType.DELETION, 0));

       
        stockRepo.deleteById(productId);
    }
}