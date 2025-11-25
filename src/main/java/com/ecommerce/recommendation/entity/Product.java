package com.ecommerce.recommendation.entity;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product extends PanacheEntityBase {
    @Id
    public Long id;
    
    public String name;
    public String description;
    public String category;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    public Product() {}
    
    public Product(Long id, String name, String description, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }
}