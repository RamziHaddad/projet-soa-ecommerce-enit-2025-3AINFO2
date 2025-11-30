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
    private BigDecimal totalMoney;// a verifier
    private OrderStatus status;
    private UUID clientId;
    @OneToMany
    private List<OrderItem> items;


}
