package com.enit.orderservice.interfaces.restcontrollers;

import com.enit.orderservice.application.service.OrderApplicationService;
import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.service.OrderDomainService;
import com.enit.orderservice.interfaces.dto.OrderMapper;
import com.enit.orderservice.interfaces.dto.OrderRequestDTO;
import com.enit.orderservice.interfaces.dto.OrderResponseDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderController {

    @Inject
    OrderApplicationService service;

    @POST
    @Path("/create_order")
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        Order order = OrderMapper.toDomain(dto);
        Order saved = service.createOrder(order);
        return OrderMapper.toResponse(saved);
    }

    @GET
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = service.getOrders();
        return orders.stream()
                .map(OrderMapper::toResponse)
                .toList();
    }
    @DELETE
    @Path("/delete_all")
    public void deleteAllOrders() {
        service.deleteAllOrders();
    }
}

