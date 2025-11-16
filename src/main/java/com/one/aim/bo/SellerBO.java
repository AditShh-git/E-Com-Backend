package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seller",
        indexes = {
                @Index(name = "idx_seller_email", columnList = "email"),
                @Index(name = "idx_seller_fullname", columnList = "fullName")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_email", columnNames = "email")
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

    private String fullName;

    private String email;

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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String resetToken;

    private LocalDateTime resetTokenExpiry;
}
