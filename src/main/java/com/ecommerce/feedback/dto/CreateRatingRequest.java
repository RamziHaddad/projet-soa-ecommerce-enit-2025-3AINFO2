package com.ecommerce.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRatingRequest {
    
    @NotNull(message = "productId est obligatoire")
    private Long productId;
    
    @NotNull(message = "score est obligatoire")
    @Min(value = 1, message = "Le score doit être entre 1 et 5")
    @Max(value = 5, message = "Le score doit être entre 1 et 5")
    private Integer score;
}

