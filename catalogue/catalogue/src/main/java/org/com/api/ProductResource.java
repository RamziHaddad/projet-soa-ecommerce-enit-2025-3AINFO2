package org.com.api;

import java.util.List;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import org.com.DTO.ProductDTO;
import org.com.entities.Product;
import org.com.exceptions.EntityAlreadyExistsException;
import org.com.exceptions.EntityNotFoundException;
import org.com.service.ProductService;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello Product Catalog";
    }

    @GET
    public List<Product> findAll() {
        return productService.getAllProducts();
    }

    @GET
    @Path("/{id}")
    public Product findById(@PathParam("id") UUID id) throws EntityNotFoundException {
        return productService.getProduct(id);
    }

    @GET
    @Path("/category/{categoryId}")
    public List<Product> findByCategory(@PathParam("categoryId") UUID categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @POST
    public Product create(@Valid ProductDTO dto) throws EntityAlreadyExistsException {
        return productService.createProduct(dto);
    }

    @PUT
    @Path("/{id}")
    public Product update(@PathParam("id") UUID id, @Valid ProductDTO dto) throws EntityNotFoundException {
        return productService.updateProduct(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") UUID id) {
        productService.deleteProduct(id);
    }
}