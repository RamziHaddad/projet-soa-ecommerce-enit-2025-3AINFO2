package com.enit.orderservice.domaine.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@Entity @Builder @NoArgsConstructor
@AllArgsConstructor @ToString
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;
    private String customerId;
    private String deliveryAddress;
    private BigDecimal totalMoney;// a verifier
    private OrderStatus status;
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    // Saga compensation tracking fields
    private String inventoryReservationId;
    private String paymentId;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }
    public void created() {
        this.status = OrderStatus.CREATED;
    }
    public void pay() {
        this.status = OrderStatus.PAID; }
    public void deliver() {
        this.status = OrderStatus.DELIVERED; }
    public void fail() {
        this.status = OrderStatus.FAILED; }

    public void recalculateTotal() {
        this.totalMoney = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
