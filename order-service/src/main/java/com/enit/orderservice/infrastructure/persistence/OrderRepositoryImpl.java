package com.enit.orderservice.infrastructure.persistence;


import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.repository.OrderRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;
@Transactional
@ApplicationScoped
public class OrderRepositoryImpl implements OrderRepository, PanacheRepository<Order> {

    @Override
    public Order save(Order order) {
        persist(order);
        return order;
    }


    @Override
    public Optional<Order> findById(UUID id) {
        return find("orderId", id).firstResultOptional();
    }
}

