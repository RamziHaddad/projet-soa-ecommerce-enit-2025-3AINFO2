package ecommerce.pricing.dto;

import java.util.List;

public class BatchPriceRequest {
    private List<Long> productIds;
    private Long userId;

    public List<Long> getProductIds() { return productIds; }
    public void setProductIds(List<Long> productIds) { this.productIds = productIds; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}