package com.enit.orderservice.interfaces.dto;



import java.util.List;

public record OrderRequestDTO(
        String customerId,
        List<OrderItemDTO> items
) {}
