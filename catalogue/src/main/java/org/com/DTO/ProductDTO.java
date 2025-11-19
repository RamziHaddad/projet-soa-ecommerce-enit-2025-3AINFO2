package org.com.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductDTO {

    @NotBlank(message = "Name is required")
    public String name;

    public String description;
    @DecimalMin(value = "0.0", inclusive = false, message = "The price must be greater than 0")

    public BigDecimal priceCatalog;

    public UUID categoryId;
    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPriceCatalog() {
        return priceCatalog;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriceCatalog(BigDecimal priceCatalog) {
        this.priceCatalog = priceCatalog;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
}
