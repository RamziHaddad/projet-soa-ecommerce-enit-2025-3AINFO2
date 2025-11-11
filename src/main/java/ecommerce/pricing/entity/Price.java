package ecommerce.pricing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "currency", length = 3)
    private String currency = "EUR";
    
    @Column(name = "effective_date")
    private LocalDate effectiveDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PriceStatus status = PriceStatus.ACTIVE;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum PriceStatus {
        ACTIVE, INACTIVE
    }
    
    // Constructors
    public Price() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.effectiveDate = LocalDate.now();
    }
    
    public Price(Long productId, BigDecimal basePrice) {
        this();
        this.productId = productId;
        this.basePrice = basePrice;
    }
    
    public Price(Long productId, BigDecimal basePrice, String currency) {
        this();
        this.productId = productId;
        this.basePrice = basePrice;
        this.currency = currency;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { 
        this.basePrice = basePrice; 
        this.lastUpdated = LocalDateTime.now();
    }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public PriceStatus getStatus() { return status; }
    public void setStatus(PriceStatus status) { this.status = status; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", productId=" + productId +
                ", basePrice=" + basePrice +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                '}';
    }
}