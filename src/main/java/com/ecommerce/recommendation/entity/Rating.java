package com.ecommerce.recommendation.entity;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ratings")
public class Rating extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Column(name = "user_id")
    public Long userId;
    
    @Column(name = "product_id")
    public Long productId;
    
    public Integer rating;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    public Rating() {}
    
    public Rating(Long userId, Long productId, Integer rating) {
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }
}