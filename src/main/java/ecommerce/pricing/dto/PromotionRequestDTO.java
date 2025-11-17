package com.ecommerce.pricing_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PromotionRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.01", message = "Discount must be at least 0.01%")
    @DecimalMax(value = "100.00", message = "Discount cannot exceed 100%")
    private BigDecimal discountPercentage;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}