package com.ecommerce.recommendation.resource;

import java.util.List;

import com.ecommerce.recommendation.entity.Order;
import com.ecommerce.recommendation.entity.Product;
import com.ecommerce.recommendation.entity.Rating;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    @Path("/products")
    public List<Product> getProducts() {
        return Product.listAll();
    }

    @GET
    @Path("/orders")
    public List<Order> getOrders() {
        return Order.listAll();
    }

    @GET
    @Path("/ratings")
    public List<Rating> getRatings() {
        return Rating.listAll();
    }
}