package com.one.aim.bo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_order_id", columnList = "orderId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_order_id", columnNames = "orderId")
        }
)
public class OrderBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "orderId", unique = true, nullable = false, updatable = false, length = 20)
    private String orderId;

    private Long totalAmount;
    private String paymentMethod;   // COD, UPI, CARD
    private LocalDateTime orderTime;

    private String orderStatus;     // INITIAL, CONFIRMED, SHIPPED, DELIVERED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserBO user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressBO shippingAddress;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "order_cart_items",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "cart_id")
    )
    private List<CartBO> cartItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_person_id")
    private DeliveryPersonBO deliveryPerson;

    private String deliveryStatus;
    private String paymentStatus;

    private String razorpayorderid;
    private String invoiceno;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------
    // Generate orderCode before insert
    // ---------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        if (this.orderTime == null) {
            this.orderTime = LocalDateTime.now();
        }
        if (this.orderId == null || this.orderId.isBlank()) {
            this.orderId = generateOrderCode();
        }
    }

    private String generateOrderCode() {
        // Format: ORD-XXXXXX (A–Z + 0–9)
        String prefix = "ORD-";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
