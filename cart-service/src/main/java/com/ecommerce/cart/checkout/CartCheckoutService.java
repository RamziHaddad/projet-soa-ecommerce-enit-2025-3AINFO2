package com.ecommerce.cart.checkout;

import com.ecommerce.cart.Cart;
import com.ecommerce.cart.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CartCheckoutService {

    @Inject
    CartService cartService;

    @Inject
    @Channel("cart-checkout-out")
    Emitter<String> checkoutEmitter;

    private ObjectMapper mapper = new ObjectMapper();

    public CheckoutResponse checkout(String userId) {

        // Get the cart
        Cart cart = cartService.getCart(userId);

        // Validate cart is not empty
        if (cart.items == null || cart.items.isEmpty()) {
            throw new IllegalStateException("Cannot checkout: Cart is empty");
        }

        // Calculate total
        double total = cart.items.stream()
                .mapToDouble(item -> item.price * item.quantity)
                .sum();

        // Generate checkout ID
        String checkoutId = UUID.randomUUID().toString();

        // Create checkout event
        List<CartCheckoutEvent.CheckoutItem> checkoutItems = cart.items.stream()
                .map(item -> new CartCheckoutEvent.CheckoutItem(
                        item.productId,
                        item.name,
                        item.quantity,
                        item.price))
                .collect(Collectors.toList());

        CartCheckoutEvent event = new CartCheckoutEvent(userId, checkoutId, checkoutItems, total);

        try {
            // Publish event to Kafka
            String eventJson = mapper.writeValueAsString(event);

            // Create metadata with key
            OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String>builder()
                    .withKey(userId)
                    .build();

            // Send message with metadata
            Message<String> message = Message.of(eventJson).addMetadata(metadata);
            checkoutEmitter.send(message);

            // Clear the cart after successful checkout
            cartService.clearCart(userId);

            return new CheckoutResponse(
                    checkoutId,
                    userId,
                    "PENDING",
                    total,
                    "Checkout initiated successfully. Your order is being processed.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Checkout failed: Unable to process order", e);
        }
    }
}
