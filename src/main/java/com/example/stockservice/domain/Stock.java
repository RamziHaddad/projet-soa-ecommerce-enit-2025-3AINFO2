package com.example.stockservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Stock {

    @Id
    private String productId;
    private int quantityTotal;    // Stock physique dans l'entrepôt
    private int quantityReserved; // Stock bloqué pour des commandes en cours

    // Optimistic Locking : Empêche deux threads de modifier le stock à la milliseconde près
    @Version
    private Long version;

    public Stock() {}

    public Stock(String productId, int initialQuantity) {
        this.productId = productId;
        this.quantityTotal = initialQuantity;
        this.quantityReserved = 0;
    }

    // --- LOGIQUE MÉTIER ---

    public int getAvailableQuantity() {
        return quantityTotal - quantityReserved;
    }

    public void reserve(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("La quantité doit être positive");
        if (getAvailableQuantity() < amount) {
            throw new IllegalStateException("Stock insuffisant. Disponible: " + getAvailableQuantity());
        }
        this.quantityReserved += amount;
    }

    public void confirmSale(int amount) {

        this.quantityTotal -= amount;
        this.quantityReserved -= amount;


        if (this.quantityReserved < 0) this.quantityReserved = 0;
        if (this.quantityTotal < 0) this.quantityTotal = 0;
    }

    public void cancelReservation(int amount) {
        this.quantityReserved -= amount;
        if (this.quantityReserved < 0) {
            this.quantityReserved = 0;
        }
    }

    // Getters
    public String getProductId() { return productId; }
    public int getQuantityTotal() { return quantityTotal; }
    public int getQuantityReserved() { return quantityReserved; }
}