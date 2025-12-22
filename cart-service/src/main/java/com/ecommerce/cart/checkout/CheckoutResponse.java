package com.ecommerce.cart.checkout;

public class CheckoutResponse {
    public String checkoutId;
    public String userId;
    public String status;
    public double totalAmount;
    public String message;

    public CheckoutResponse() {
    }

    public CheckoutResponse(String checkoutId, String userId, String status, double totalAmount, String message) {
        this.checkoutId = checkoutId;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.message = message;
    }
}
