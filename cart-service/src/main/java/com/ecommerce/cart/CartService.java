package com.ecommerce.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.redis.datasource.value.SetArgs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class CartService {

    @Inject
    RedisDataSource redisDataSource;

    @ConfigProperty(name = "cart.redis.ttl", defaultValue = "86400")
    long redisTtlSeconds;

    private ObjectMapper mapper = new ObjectMapper();

    private ValueCommands<String, String> valueCommands;

    @jakarta.annotation.PostConstruct
    void init() {
        valueCommands = redisDataSource.value(String.class);
    }

    public Cart getCart(String userId) {
        try {
            String json = valueCommands.get("cart:" + userId);
            if (json != null && !json.isEmpty()) {
                Cart cart = mapper.readValue(json, Cart.class);
                return cart;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve cart", e);
        }
        return new Cart(userId);
    }

    public void saveCart(Cart cart) {
        try {
            String cartKey = "cart:" + cart.userId;
            String cartJson = mapper.writeValueAsString(cart);

            // Set with TTL to auto-expire abandoned carts
            SetArgs setArgs = new SetArgs().ex(Duration.ofSeconds(redisTtlSeconds));
            valueCommands.set(cartKey, cartJson, setArgs);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save cart", e);
        }
    }

    public Cart addItem(String userId, CartItem item) {
        if (item.quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Cart cart = getCart(userId);
        cart.addItem(item);
        saveCart(cart);
        return cart;
    }

    public Cart removeItem(String userId, String productId) {
        Cart cart = getCart(userId);
        cart.removeItem(productId);
        saveCart(cart);
        return cart;
    }

    public Cart updateQuantity(String userId, String productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (quantity == 0) {
            return removeItem(userId, productId);
        }

        Cart cart = getCart(userId);
        cart.updateQuantity(productId, quantity);
        saveCart(cart);
        return cart;
    }

    public void clearCart(String userId) {
        var keyCommands = redisDataSource.key();
        keyCommands.del("cart:" + userId);
    }

    public double calculateTotal(String userId) {
        Cart cart = getCart(userId);
        return cart.items.stream()
                .mapToDouble(item -> item.price * item.quantity)
                .sum();
    }
}