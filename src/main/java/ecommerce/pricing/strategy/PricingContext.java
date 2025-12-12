package ecommerce.pricing.strategy;

/**
 * Contexte contenant toutes les informations nécessaires au calcul de prix
 * Utilisé par les stratégies pour prendre leurs décisions
 */
public class PricingContext {
    
    private Long productId;
    private Long userId;
    private Integer quantity;
    private String customerType;  // "REGULAR", "VIP", "WHOLESALE"
    private String seasonalPeriod; // "NORMAL", "BLACK_FRIDAY", "CHRISTMAS", "SUMMER_SALE"
    
    // Constructeur par défaut
    public PricingContext() {
        this.quantity = 1;
        this.customerType = "REGULAR";
        this.seasonalPeriod = "NORMAL";
    }
    
    // Constructeur avec les paramètres essentiels
    public PricingContext(Long productId, Long userId) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = 1;
        this.customerType = "REGULAR";
        this.seasonalPeriod = "NORMAL";
    }
    
    // Getters et Setters
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getCustomerType() {
        return customerType;
    }
    
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }
    
    public String getSeasonalPeriod() {
        return seasonalPeriod;
    }
    
    public void setSeasonalPeriod(String seasonalPeriod) {
        this.seasonalPeriod = seasonalPeriod;
    }

    @Override
    public String toString() {
        return "PricingContext{" +
                "productId=" + productId +
                ", userId=" + userId +
                ", quantity=" + quantity +
                ", customerType='" + customerType + '\'' +
                ", seasonalPeriod='" + seasonalPeriod + '\'' +
                '}';
    }
}