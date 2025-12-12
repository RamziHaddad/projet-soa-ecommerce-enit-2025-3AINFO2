package com.example.stockservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StockRequest(
        @NotBlank(message = "L'ID de commande est obligatoire")
        String orderId,

        @Min(value = 1, message = "La quantité doit être au moins 1")
        int quantity
) {}