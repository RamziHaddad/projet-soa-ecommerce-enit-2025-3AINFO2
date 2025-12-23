package com.enit.orderservice.infrastructure.persistence;

import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.repository.OrderRepository;
import com.enit.orderservice.infrastructure.exception.PersistenceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of OrderRepository
 * Handles database persistence operations for Order entities
 */
@ApplicationScoped
public class OrderRepositoryImpl implements OrderRepository {

    private static final Logger LOG = Logger.getLogger(OrderRepositoryImpl.class);

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public Order save(Order order) {
        try {
            if (order.getOrderId() == null) {
                // New order - persist
                entityManager.persist(order);
                LOG.infof("Persisted new order: %s", order.getOrderId());
                return order;
            } else {
                // Existing order - merge (update)
                Order merged = entityManager.merge(order);
                LOG.infof("Updated order: %s", order.getOrderId());
                return merged;
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to save order: %s", order.getOrderId());
            throw new PersistenceException("save", "Order", e);
        }
    }

    @Override
    public Optional<Order> findById(UUID id) {
        try {
            Order order = entityManager.find(Order.class, id);
            return Optional.ofNullable(order);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to find order by id: %s", id);
            throw new PersistenceException("findById", "Order", e);
        }
    }

    @Override
    public List<Order> listAll() {
        try {
            return entityManager
                    .createQuery("SELECT o FROM Order o", Order.class)
                    .getResultList();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to list all orders");
            throw new PersistenceException("listAll", "Order", e);
        }
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
