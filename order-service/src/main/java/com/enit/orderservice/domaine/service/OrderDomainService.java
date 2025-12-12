package com.enit.orderservice.domaine.service;



import com.enit.orderservice.domaine.model.Order;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderDomainService {

    public void validateNewOrder(Order order) {

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have items");
        }

        if (order.getTotalMoney() == null) {
            order.recalculateTotal(); // safety
        }

        if (order.getTotalMoney().signum() <= 0) {
            throw new IllegalArgumentException("Total amount must be > 0");
        }
    }
}

