package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(
        name = "seller",
        indexes = {
                @Index(name = "idx_seller_email", columnList = "email"),
                @Index(name = "idx_seller_fullname", columnList = "fullName"),
                @Index(name = "idx_seller_seller_id", columnList = "sellerId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_seller_seller_id", columnNames = "sellerId")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SellerBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "sellerId", unique = true, nullable = false, updatable = false, length = 20)
    private String sellerId;

    private String fullName;

    private String email;

    @Column(length = 15, unique = true)
    private String phoneNo;

    private String gst;
    private String adhaar;
    private String panCard;

    private String password;

    @Builder.Default
    private boolean verified = false;

    @Builder.Default
    private boolean login = false;

    @Builder.Default
    private String role = "SELLER";

    private Long imageFileId;

    @Builder.Default
    private boolean emailVerified = false;

    private String verificationToken;

    private LocalDateTime verificationTokenExpiry;

    private String pendingEmail;

    @Builder.Default
    private boolean locked = true;

    @Builder.Default
    private boolean rejected = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    @Builder.Default
    private boolean active = true;

    // ---------------------------------------------------------
    // Generate sellerCode before insert
    // ---------------------------------------------------------
    @PrePersist
    public void prePersist() {
        // keep existing @Builder defaults etc.
        if (this.sellerId == null || this.sellerId.isBlank()) {
            this.sellerId = generateSellerCode();
        }
    }

    private String generateSellerCode() {
        // Format: SLR-XXXXXX (digits only)
        StringBuilder sb = new StringBuilder("SLR-");
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < 6; i++) {
            sb.append(rnd.nextInt(0, 10));
        }
        return sb.toString();
    }
}
