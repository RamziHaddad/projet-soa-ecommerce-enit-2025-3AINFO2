package com.enit.orderservice;

import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.model.OrderStatus;
import com.enit.orderservice.domaine.repository.OrderRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/hello")
public class ExampleResource {
    @Inject
    OrderRepository orderRepository;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String hello() {
//        Order order = Order.builder().customerId("edededd").status(OrderStatus.Created).build();
//        orderRepository.persist(order);
//
//        List<Order> all = orderRepository.findAll().list();
//        System.out.println(all);
//        return "Hello from Quarkus REST"+all;
        return "Hello from Quarkus REST";
    }
}
