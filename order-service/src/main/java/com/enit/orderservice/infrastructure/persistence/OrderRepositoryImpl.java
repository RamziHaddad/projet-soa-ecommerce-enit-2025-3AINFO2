package com.enit.orderservice.infrastructure.persistence;

import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of OrderRepository
 * Handles database persistence operations for Order entities
 */
@ApplicationScoped
public class OrderRepositoryImpl implements OrderRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public Order save(Order order) {
        if (order.getOrderId() == null) {
            // New order - persist
            entityManager.persist(order);
            return order;
        } else {
            // Existing order - merge (update)
            return entityManager.merge(order);
        }
    }

    @Override
    public Optional<Order> findById(UUID id) {
        Order order = entityManager.find(Order.class, id);
        return Optional.ofNullable(order);
    }

    @Override
    public List<Order> listAll() {
        return entityManager
                .createQuery("SELECT o FROM Order o", Order.class)
                .getResultList();
    }

    @Override
    @Transactional
    public void delete(Order order) {
        // Ensure order is managed before removing
        if (!entityManager.contains(order)) {
            order = entityManager.merge(order);
        }
        entityManager.remove(order);
    }
}
