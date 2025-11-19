package org.com.service;

import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.com.DTO.ProductDTO;
import org.com.entities.Product;
import org.com.exceptions.EntityAlreadyExistsException;
import org.com.exceptions.EntityNotFoundException;
import org.com.repository.ProductRepository;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    public Product createProduct(ProductDTO dto) throws EntityAlreadyExistsException {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPriceCatalog(dto.getPriceCatalog());
        product.setCategoryId(dto.getCategoryId());

        return productRepository.insert(product);
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

    public Product updateProduct(UUID id, ProductDTO dto) throws EntityNotFoundException {
        Product product = productRepository.findById(id);

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPriceCatalog(dto.getPriceCatalog());
        product.setCategoryId(dto.getCategoryId());

        return productRepository.update(product);
    }

    public void deleteProduct(UUID id) {
        productRepository.delete(id);
    }
}