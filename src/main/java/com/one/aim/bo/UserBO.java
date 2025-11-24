package com.one.aim.bo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

    @Column(length = 15, unique = true)
    private String phoneNo;

    @Column(nullable = false)
    private String password;

    private String role;   // USER / SELLER / ADMIN

    // =====================================================
    // EMAIL VERIFICATION
    // =====================================================
    @Builder.Default
    private Boolean emailVerified = false;

    private String pendingEmail;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;

    // =====================================================
    // PASSWORD RESET
    // =====================================================
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // =====================================================
    // ACCOUNT STATUS & SOFT DELETE
    // =====================================================
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean deleted = false;

    @Builder.Default
    private Boolean loggedIn = false;

    public boolean isActive() {
        return Boolean.TRUE.equals(active) && Boolean.FALSE.equals(deleted);
    }

    // =====================================================
    // AUDIT FIELDS
    // =====================================================
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // PROFILE IMAGE
    // =====================================================
    private Long imageFileId;

    // =====================================================
    // USER → CART
    // =====================================================
    @OneToMany(mappedBy = "userAddToCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartBO> addtoCart = new ArrayList<>();

    // =====================================================
    // USER → WISHLIST
    // =====================================================
    @ManyToMany
    @JoinTable(
            name = "user_wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<ProductBO> wishlistProducts = new ArrayList<>();
}
