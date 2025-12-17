package enit.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "deliveries",
        indexes = {
                @Index(name = "idx_delivery_user", columnList = "userId"),
                @Index(name = "idx_delivery_order", columnList = "orderId")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;
    private LocalDateTime deliveredAt;

    @Version
    private Long version;

    public static Delivery create(Long orderId, Long userId, String address) {
        Delivery d = new Delivery();
        d.orderId = orderId;
        d.userId = userId;
        d.address = address;
        d.status = DeliveryStatus.CREATED;
        d.createdAt = LocalDateTime.now();
        return d;
    }


    /* ===== Business Rules ===== */
    public void start() {
        if (this.status != DeliveryStatus.CREATED) {
            throw new IllegalStateException("Delivery cannot be started");
        }
        this.status = DeliveryStatus.IN_TRANSIT;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException("Delivery cannot be completed");
        }
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
}
