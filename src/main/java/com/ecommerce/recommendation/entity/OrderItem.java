package com.ecommerce.recommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OrderItem {
    @Column(name = "product_id")
    public Long productId;
    
    public Integer quantity;
    
    public OrderItem() {}
    
    public OrderItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
}