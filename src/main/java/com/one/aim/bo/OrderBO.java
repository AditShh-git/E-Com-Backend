package com.one.aim.bo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Table(name = "orders")
public class OrderBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long totalAmount;
    private String paymentMethod;   // COD, UPI, CARD
    private LocalDateTime orderTime;

    private String orderStatus;     // INITIAL, CONFIRMED, SHIPPED, DELIVERED

    // REMOVED FIELD: cartempids
    // Reason: CartBO no longer stores sellerId after redesign.
    // Seller is now derived directly from ProductBO → SellerBO.
    // Keeping seller IDs in OrderBO became duplicate + incorrect.

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
}
