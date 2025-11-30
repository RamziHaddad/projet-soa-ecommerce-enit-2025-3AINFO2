package com.ecommerce.feedback.exception;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(Long productId) {
        super(String.format("Produit avec l'id %d n'a pas été trouvé dans le service Catalog", productId));
    }
}

