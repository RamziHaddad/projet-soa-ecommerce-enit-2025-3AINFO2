package com.ecommerce.payment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.payment.client.OrderClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class OrderServiceCaller {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceCaller.class);

    private final OrderClient orderClient;

    public OrderServiceCaller(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "orderFallback")
    public void updateOrderStatus(Long orderId, String status) {
        orderClient.updateOrderStatus(orderId, status);
    }

    public void orderFallback(Long orderId, String status, Exception e) {
        log.error("Order Service unavailable (Circuit Breaker OPEN). OrderId={}, status={}",
                orderId, status, e);
        throw new RuntimeException("Order service unavailable");
    }
}
