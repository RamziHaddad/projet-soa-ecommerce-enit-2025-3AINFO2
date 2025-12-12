package com.enit.orderservice.domaine.repository;

import com.enit.orderservice.domaine.model.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);
    Optional<Order> findById(UUID id);
}
