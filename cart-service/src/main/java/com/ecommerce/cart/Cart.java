package com.ecommerce.cart;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    public String userId;
    public List<CartItem> items = new ArrayList<>();

    // No-arg constructor for Jackson deserialization
    public Cart() {
    }

    public Cart(String userId) {
        this.userId = userId;
    }

    public void addItem(CartItem item) {
        items.add(item);
    }

    public void removeItem(String productId) {
        items.removeIf(i -> i.productId.equals(productId));
    }

    public void updateQuantity(String productId, int quantity) {
        for (CartItem i : items) {
            if (i.productId.equals(productId)) {
                i.quantity = quantity;
                break;
            }
        }
    }
}
