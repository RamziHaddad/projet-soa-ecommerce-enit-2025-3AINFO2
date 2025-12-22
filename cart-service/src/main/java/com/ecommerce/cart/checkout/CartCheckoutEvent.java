package com.ecommerce.cart.checkout;

import java.time.LocalDateTime;
import java.util.List;

public class CartCheckoutEvent {
    public String userId;
    public String checkoutId;
    public List<CheckoutItem> items;
    public double totalAmount;
    public LocalDateTime timestamp;

    public CartCheckoutEvent() {
    }

    public CartCheckoutEvent(String userId, String checkoutId, List<CheckoutItem> items, double totalAmount) {
        this.userId = userId;
        this.checkoutId = checkoutId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.timestamp = LocalDateTime.now();
    }

    public static class CheckoutItem {
        public String productId;
        public String name;
        public int quantity;
        public double price;
        public double subtotal;

        public CheckoutItem() {
        }

        public CheckoutItem(String productId, String name, int quantity, double price) {
            this.productId = productId;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = quantity * price;
        }
    }
}
