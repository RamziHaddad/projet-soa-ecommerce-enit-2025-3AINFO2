package com.enit.orderservice.interfaces.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID orderId,
        String customerId,
        String deliveryAddress,
        BigDecimal totalAmount,
        String status,
        List<OrderItemDTO> items
) {}

