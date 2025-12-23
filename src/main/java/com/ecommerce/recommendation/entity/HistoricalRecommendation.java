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
@Table(name = "historical_recommendations")
public class HistoricalRecommendation extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "category_recommended")
    public String categoryRecommended;

    @Column(name = "product_recommended_id")
    public Long productRecommendedId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    public HistoricalRecommendation() {
    }

    public HistoricalRecommendation(Long userId, String categoryRecommended, Long productRecommendedId) {
        this.userId = userId;
        this.categoryRecommended = categoryRecommended;
        this.productRecommendedId = productRecommendedId;
        this.createdAt = LocalDateTime.now();
    }
}
