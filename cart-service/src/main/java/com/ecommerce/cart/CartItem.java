package com.ecommerce.cart;

public class CartItem {
    public String productId;
    public String name;
    public int quantity;
    public double price;


    public CartItem(String productId, String name, int quantity, double price) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
}
