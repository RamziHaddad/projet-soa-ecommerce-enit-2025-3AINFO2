package com.ecommerce.feedback.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "productId est obligatoire")
    private Long productId;

    @NotNull(message = "Le contenu est obligatoire")
    @Size(min = 10, max = 1000, message = "Le contenu doit contenir entre 10 et 1000 caractères")
    private String content;

    // Optionnel: lien vers un rating existant
    private Long ratingId;
}
