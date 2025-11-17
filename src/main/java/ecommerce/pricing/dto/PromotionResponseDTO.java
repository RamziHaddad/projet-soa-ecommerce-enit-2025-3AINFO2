package com.ecommerce.pricing_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PromotionResponseDTO {
    private Long id;
    private Long productId;
    private BigDecimal discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private boolean isActive;
}