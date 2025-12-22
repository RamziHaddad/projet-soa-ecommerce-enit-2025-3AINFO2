package com.ecommerce.cart;

import com.ecommerce.cart.checkout.CartCheckoutService;
import com.ecommerce.cart.checkout.CheckoutResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    @Inject
    CartService cartService;

    @Inject
    CartCheckoutService checkoutService;

    @GET
    @Path("/{userId}")
    public Response getCart(@PathParam("userId") String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            Cart cart = cartService.getCart(userId);
            return Response.ok(cart).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to retrieve cart"))
                    .build();
        }
    }

    @POST
    @Path("/{userId}/items")
    public Response addItem(@PathParam("userId") String userId, CartItem item) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            if (item == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Cart item is required"))
                        .build();
            }

            if (item.productId == null || item.productId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Product ID is required"))
                        .build();
            }

            if (item.quantity <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Quantity must be positive"))
                        .build();
            }

            Cart cart = cartService.addItem(userId, item);
            return Response.ok(cart).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to add item to cart"))
                    .build();
        }
    }

    @PUT
    @Path("/{userId}/items/{productId}")
    public Response updateQuantity(@PathParam("userId") String userId,
            @PathParam("productId") String productId,
            @QueryParam("quantity") int quantity) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            if (productId == null || productId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Product ID is required"))
                        .build();
            }

            Cart cart = cartService.updateQuantity(userId, productId, quantity);
            return Response.ok(cart).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to update quantity"))
                    .build();
        }
    }

    @DELETE
    @Path("/{userId}/items/{productId}")
    public Response removeItem(@PathParam("userId") String userId,
            @PathParam("productId") String productId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            if (productId == null || productId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Product ID is required"))
                        .build();
            }

            Cart cart = cartService.removeItem(userId, productId);
            return Response.ok(cart).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to remove item"))
                    .build();
        }
    }

    @DELETE
    @Path("/{userId}")
    public Response clearCart(@PathParam("userId") String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            cartService.clearCart(userId);
            return Response.ok(Map.of("message", "Cart cleared successfully")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to clear cart"))
                    .build();
        }
    }

    @POST
    @Path("/{userId}/checkout")
    public Response checkout(@PathParam("userId") String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            CheckoutResponse response = checkoutService.checkout(userId);
            return Response.ok(response).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Checkout failed"))
                    .build();
        }
    }

    @GET
    @Path("/{userId}/total")
    public Response getTotal(@PathParam("userId") String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            double total = cartService.calculateTotal(userId);
            return Response.ok(Map.of("userId", userId, "total", total)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to calculate total"))
                    .build();
        }
    }
}
