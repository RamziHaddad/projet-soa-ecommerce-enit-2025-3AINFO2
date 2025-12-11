package com.ecommerce.cart;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    @Inject
    CartService cartService;

    @GET
    @Path("/{userId}")
    public Cart getCart(@PathParam("userId") String userId) {
        return cartService.getCart(userId);
    }

    @POST
    @Path("/{userId}/items")
    public Cart addItem(@PathParam("userId") String userId, CartItem item) {
        return cartService.addItem(userId, item);
    }

    @PUT
    @Path("/{userId}/items/{productId}")
    public Cart updateQuantity(@PathParam("userId") String userId,
                               @PathParam("productId") String productId,
                               int quantity) {
        return cartService.updateQuantity(userId, productId, quantity);
    }

    @DELETE
    @Path("/{userId}/items/{productId}")
    public Cart removeItem(@PathParam("userId") String userId,
                           @PathParam("productId") String productId) {
        return cartService.removeItem(userId, productId);
    }

    @DELETE
    @Path("/{userId}")
    public void clearCart(@PathParam("userId") String userId) {
        cartService.clearCart(userId);
    }
}
