package com.ecommerce.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummaryResponse {
    
    private Long productId;
    private Double average; // Moyenne des notes
    private Long ratingsCount; // Nombre total de notes
}

