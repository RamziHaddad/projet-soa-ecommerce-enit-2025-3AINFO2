package com.example.stockservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity // Dit à Spring : "Ceci est une table dans la base de données"
public class Stock {

    @Id // Dit à Spring : "C'est la clé primaire"
    private String productId;
    private int quantityTotal;
    private int quantityReserved;

    // Constructeur vide obligatoire pour JPA (la base de données)
    public Stock() {}

    // Constructeur pour nous
    public Stock(String productId, int initialQuantity) {
        this.productId = productId;
        this.quantityTotal = initialQuantity;
        this.quantityReserved = 0;
    }

    // --- LOGIQUE MÉTIER (POURQUOI CE CODE EST ICI ?) ---
    // En architecture hexagonale, l'intelligence est dans l'objet lui-même.
    // On ne veut pas que le Service fasse des calculs, l'objet Stock sait se gérer.

    public int getAvailableQuantity() {
        return quantityTotal - quantityReserved;
    }

    public void reserve(int amount) {
        if (getAvailableQuantity() < amount) {
            throw new RuntimeException("Stock insuffisant !");
        }
        this.quantityReserved += amount;
    }

    public void confirmSale(int amount) {
        this.quantityTotal -= amount;      // On retire physiquement du stock
        this.quantityReserved -= amount;   // On retire de la réservation
    }

    public void cancelReservation(int amount) {
        this.quantityReserved -= amount;   // On remet en circulation
    }

    // Getters nécessaires pour que le framework lise les valeurs
    public String getProductId() { return productId; }
    public int getQuantityTotal() { return quantityTotal; }
    public int getQuantityReserved() { return quantityReserved; }
}