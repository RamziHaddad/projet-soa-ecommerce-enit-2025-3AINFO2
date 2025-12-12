package ecommerce.pricing.dto;

/**
 * DTO pour les requêtes de calcul de prix avec stratégie
 * Contient toutes les informations nécessaires pour calculer un prix
 */
public class StrategyPriceRequest {
    
    private Long productId;
    private Long userId;
    private Integer quantity;
    private String strategyName;    // "BASE", "STANDARD", "WHOLESALE", "VIP", "SEASONAL"
    private String customerType;    // "REGULAR", "VIP", "WHOLESALE"
    private String seasonalPeriod;  // "NORMAL", "BLACK_FRIDAY", "CHRISTMAS", "SUMMER_SALE"
    
    // Constructeur par défaut
    public StrategyPriceRequest() {
        this.quantity = 1;
        this.customerType = "REGULAR";
        this.seasonalPeriod = "NORMAL";
    }
    
    // Constructeur avec paramètres essentiels
    public StrategyPriceRequest(Long productId, Long userId) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = 1;
        this.customerType = "REGULAR";
        this.seasonalPeriod = "NORMAL";
    }
    
    // Constructeur complet
    public StrategyPriceRequest(Long productId, Long userId, Integer quantity, 
                                String strategyName, String customerType, String seasonalPeriod) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.strategyName = strategyName;
        this.customerType = customerType;
        this.seasonalPeriod = seasonalPeriod;
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
        return quantity != null ? quantity : 1;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getStrategyName() {
        return strategyName;
    }
    
    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }
    
    public String getCustomerType() {
        return customerType != null ? customerType : "REGULAR";
    }
    
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }
    
    public String getSeasonalPeriod() {
        return seasonalPeriod != null ? seasonalPeriod : "NORMAL";
    }
    
    public void setSeasonalPeriod(String seasonalPeriod) {
        this.seasonalPeriod = seasonalPeriod;
    }

    @Override
    public String toString() {
        return "StrategyPriceRequest{" +
                "productId=" + productId +
                ", userId=" + userId +
                ", quantity=" + quantity +
                ", strategyName='" + strategyName + '\'' +
                ", customerType='" + customerType + '\'' +
                ", seasonalPeriod='" + seasonalPeriod + '\'' +
                '}';
    }
}