package com.ecommerce.recommendation.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order extends PanacheEntityBase {
    @Id
    public Long id;
    
    @Column(name = "user_id")
    public Long userId;
    
    @Column(name = "order_date")
    public LocalDateTime orderDate;
    
    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    public List<OrderItem> items = new ArrayList<>();
    
    public Order() {}
    
    public Order(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
        this.orderDate = LocalDateTime.now();
    }
    
    public void addItem(Long productId, Integer quantity) {
        this.items.add(new OrderItem(productId, quantity));
    }
}