package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;          // SALE, ORDER_UPDATE etc.
    private String title;

    @Column(length = 500)
    private String description;

    private Long imageFileId;
    private Long redirectRefId;
    private String redirectUrl;

    private String targetRole;    // USER / SELLER / ADMIN / ALL

    @CreationTimestamp
    private LocalDateTime createdAt;


    private LocalDateTime expiryAt;
}

