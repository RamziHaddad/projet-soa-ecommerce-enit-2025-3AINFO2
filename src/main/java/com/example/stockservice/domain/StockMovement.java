package com.example.stockservice.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productId;


    // Dans le cas d'un ajustement manuel, on stockera la "Raison" ici.
    private String orderId;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private int quantity;
    private LocalDateTime timestamp;

    public StockMovement() {}

    public StockMovement(String productId, String orderId, MovementType type, int quantity) {
        this.productId = productId;
        this.orderId = orderId;
        this.type = type;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }

    // --- MISE À JOUR DE L'ENUM ---
    public enum MovementType {
        INIT,       // Création initiale
        RESERVE,    // Réservation client
        CONFIRM,    // Vente confirmée
        CANCEL,     // Annulation
        ADJUSTMENT, // <--- NOUVEAU : Correction manuelle (Casse, Vol, Inventaire)
        DELETION    // <--- NOUVEAU : Suppression du produit du catalogue
    }

    // Getters (utiles pour l'affichage ou le debug)
    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getOrderId() { return orderId; }
    public MovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getTimestamp() { return timestamp; }
}