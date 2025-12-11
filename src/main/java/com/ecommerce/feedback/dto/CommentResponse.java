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
public class CommentResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private String content;
    private Long ratingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
