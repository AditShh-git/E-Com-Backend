package com.one.aim.bo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_verification_token", columnList = "verificationToken"),
                @Index(name = "idx_reset_token", columnList = "resetToken")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15,unique = true)
    private String phoneNo;

    @Column(nullable = false)
    private String password;

    private String role; // USER, ADMIN, VENDOR, etc.

    // ===========================================================
    // EMAIL VERIFICATION
    // ===========================================================
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "pending_email")
    private String pendingEmail;

    @Column(length = 255)
    private String verificationToken;

    private LocalDateTime verificationTokenExpiry;

    // ===========================================================
    // PASSWORD RESET
    // ===========================================================
    @Column(length = 255)
    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    // ===========================================================
    // ACCOUNT STATUS / SOFT DELETE
    // ===========================================================
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean login = false; //  Add this for login tracking

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    // ===========================================================
    // AUDIT FIELDS
    // ===========================================================
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===========================================================
    // PROFILE IMAGE
    // ===========================================================
    @Column(name = "image")
    private Long imageFileId;


    // REMOVED: Old User → Cart mapping `mappedBy = "userCart"`
// Reason: CartBO no longer has a field named `userCart`.
// Keeping it caused Hibernate startup failure.
// Fix: We now use only `userAddToCart` as the correct relation.

    // Add cart by ADMIN,SELLER,VENDOR
//	@OneToMany(mappedBy = "userCart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	private List<CartBO> cartItems = new ArrayList<>();

    // ============================================
    // USER ADD-TO-CART (One User → Many Carts)
    // ============================================
    @OneToMany(mappedBy = "userAddToCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartBO> addtoCart = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<ProductBO> wishlistProducts = new ArrayList<>();



//	@Lob
//	@Column(name = "image", columnDefinition = "LONGBLOB")
//	private byte[] image;

}