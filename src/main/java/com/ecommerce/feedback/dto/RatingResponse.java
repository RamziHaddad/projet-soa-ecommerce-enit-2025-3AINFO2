package com.ecommerce.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {
    
    private Long id;
    private Long productId;
    private Long userId;
    private Integer score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

