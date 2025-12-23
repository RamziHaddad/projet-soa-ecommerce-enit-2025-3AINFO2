package com.enit.orderservice.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDTO(
        @NotNull(message = "Product ID is required")
        UUID productId,
        
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price
) {}
