package ecommerce.pricing.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderValidationResponse {
    private boolean valid;
    private BigDecimal totalAmount;
    private String currency;
    private List<ItemPrice> itemPrices;
    private String message;

    public static class ItemPrice {
        private Long productId;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;

        public ItemPrice(Long productId, BigDecimal unitPrice, Integer quantity, BigDecimal totalPrice) {
            this.productId = productId;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }

        public Long getProductId() { return productId; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getTotalPrice() { return totalPrice; }
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public List<ItemPrice> getItemPrices() { return itemPrices; }
    public void setItemPrices(List<ItemPrice> itemPrices) { this.itemPrices = itemPrices; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}