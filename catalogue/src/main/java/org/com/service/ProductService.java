package org.com.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.com.DTO.ProductDTO;
import org.com.entities.Product;
import org.com.exceptions.EntityAlreadyExistsException;
import org.com.exceptions.EntityNotFoundException;
import org.com.repository.ProductRepository;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;
    
    @Inject
    OutboxService outboxService;
    
    @Transactional
    public Product createProduct(ProductDTO dto) throws EntityAlreadyExistsException {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPriceCatalog(dto.getPriceCatalog());
        product.setCategoryId(dto.getCategoryId());

        Product savedProduct = productRepository.insert(product);
        // Créer un événement Outbox
        outboxService.createProductEvent(savedProduct, "ProductCreated");
        
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(UUID id, ProductDTO dto) throws EntityNotFoundException {
        Product product = productRepository.findById(id);

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPriceCatalog(dto.getPriceCatalog());
        product.setCategoryId(dto.getCategoryId());

       Product updatedProduct = productRepository.update(product);
        
        // Créer un événement Outbox
        outboxService.createProductEvent(updatedProduct, "ProductUpdated");
        
        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(UUID id) throws EntityNotFoundException {
        Product product = productRepository.findById(id);
        productRepository.delete(id);
        
        // Créer un événement Outbox
        outboxService.createProductEvent(product, "ProductDeleted");
    }

    
    // ceci la méthode pour modifier le prix d'un produit de catalogue 
    // déja idempotent car (id, newprice, résultat identique)
    @Transactional
    public Product updateProductPrice(UUID id, BigDecimal newPrice) throws EntityNotFoundException {
    Product product = productRepository.findById(id);
    product.setPriceCatalog(newPrice);
                return productRepository.update(product);
    }


    public Product getProduct(UUID id) throws EntityNotFoundException {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategory(categoryId);
    }


}