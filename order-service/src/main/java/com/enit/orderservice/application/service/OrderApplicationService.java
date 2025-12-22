package com.enit.orderservice.application.service;


import com.enit.orderservice.application.saga.OrderSagaOrchestrator;
import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.repository.OrderRepository;
import com.enit.orderservice.domaine.service.OrderDomainService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class OrderApplicationService {

    @Inject
    OrderRepository orderRepository;
    
    @Inject
    OrderDomainService domainService;
    
    @Inject
    OrderSagaOrchestrator sagaOrchestrator;
    /**
     * Create a new order and start the saga workflow
     * The saga will handle pricing, inventory, payment, and delivery asynchronously
     */
    @Transactional
    public Order createOrder(Order order) {
        // 1. Validate business rules
        domainService.validateNewOrder(order);
        
        // 2. Set initial order state
        order.created();
        
        // 3. Persist order to database
        orderRepository.save(order);
        
        // 4. Start async saga workflow (pricing → inventory → payment → delivery)
        sagaOrchestrator.startSaga(order.getOrderId());
        
        return order;
    }
    /**
     * Retrieve all orders
     */
    public List<Order> getOrders() {
        return orderRepository.listAll();
    }
    
    /**
     * Delete all orders (useful for testing)
     */
    public void deleteAllOrders() {
        List<Order> orders = orderRepository.listAll();
        for (Order order : orders) {
            orderRepository.delete(order);
        }
    }

}
