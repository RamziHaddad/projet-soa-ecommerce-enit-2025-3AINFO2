package com.enit.orderservice.application.service;


import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.service.OrderDomainService;

import com.enit.orderservice.infrastructure.persistence.OrderRepositoryImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class OrderApplicationService {

    @Inject
    OrderRepositoryImpl orderRepository;
    @Inject
    OrderDomainService domainService;

//    @Inject
//    @RestClient
//    InventoryClient inventoryClient;
//
//    @Inject
//    @RestClient
//    PaymentClient paymentClient;
    @Transactional
    public Order createOrder(Order order) {

        domainService.validateNewOrder(order);
        order.created();
        orderRepository.save(order);

        try {

//            inventoryClient.reserveStock(order);
//            paymentClient.processPayment(order);

            order.pay();
            orderRepository.save(order);

        } catch (Exception e) {


            try {
//                inventoryClient.releaseStock(order);
            } catch (Exception ignored) { }

            order.fail();
            orderRepository.save(order);

            throw new RuntimeException("Order failed: " + e.getMessage());
        }

        return order;
    }
    public List<Order> getOrders() {
        return orderRepository.listAll();
    }
    public void DeleteAllOrders() {
//        orderRepository.deleteAll();
        List<Order> orders = orderRepository.listAll();
        for (Order order : orders) {
            orderRepository.delete(order);
        }
    }

}
