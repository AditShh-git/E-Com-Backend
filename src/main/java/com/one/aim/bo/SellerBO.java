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

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String phoneNo;

    private String gst;
    private String adhaar;
    private String panCard;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false; //  renamed from isVarified

    @Column(nullable = false)
    @Builder.Default
    private boolean login = false;

    @Column(nullable = false)
    @Builder.Default
    private String role = "SELLER"; //  renamed from roll

//    @Lob
//    @Column(name = "image", columnDefinition = "LONGBLOB")
//    private byte[] image;

//      Store file reference instead
    @Column(name = "image_file_id")
    private Long imageFileId;

    // ===========================================================
    // AUDIT FIELDS
    // ===========================================================
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ===========================================================
    // PASSWORD RESET
    // ===========================================================
    @Column(length = 255)
    private String resetToken;

    private LocalDateTime resetTokenExpiry;

}

