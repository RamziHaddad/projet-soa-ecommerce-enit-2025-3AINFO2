package com.ecommerce.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CartService {

    @Inject
    RedisDataSource redisDataSource;

    private ObjectMapper mapper = new ObjectMapper();

    private ValueCommands<String, String> valueCommands;

    @jakarta.annotation.PostConstruct
    void init() {
        valueCommands = redisDataSource.value(String.class);
    }

    public Cart getCart(String userId) {
        try {
            String json = valueCommands.get("cart:" + userId);
            if(json != null && !json.isEmpty()) {
                return mapper.readValue(json, Cart.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Cart(userId);
    }

    public void saveCart(Cart cart) {
        try {
            valueCommands.set("cart:" + cart.userId, mapper.writeValueAsString(cart));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cart addItem(String userId, CartItem item){
        Cart cart = getCart(userId);
        cart.addItem(item);
        saveCart(cart);
        return cart;
    }

    public Cart removeItem(String userId, String productId){
        Cart cart = getCart(userId);
        cart.removeItem(productId);
        saveCart(cart);
        return cart;
    }

    public Cart updateQuantity(String userId, String productId, int quantity){
        Cart cart = getCart(userId);
        cart.updateQuantity(productId, quantity);
        saveCart(cart);
        return cart;
    }

    public void clearCart(String userId){
        var keyCommands = redisDataSource.key();
        keyCommands.del("cart:" + userId);
    }
}