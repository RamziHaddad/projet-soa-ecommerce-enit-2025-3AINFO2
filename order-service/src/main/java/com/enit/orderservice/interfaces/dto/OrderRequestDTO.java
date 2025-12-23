package com.enit.orderservice.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDTO(
        @NotBlank(message = "Customer ID is required")
        String customerId,
        
        @NotBlank(message = "Delivery address is required")
        @Size(min = 10, max = 500, message = "Delivery address must be between 10 and 500 characters")
        String deliveryAddress,
        
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemDTO> items
) {}
