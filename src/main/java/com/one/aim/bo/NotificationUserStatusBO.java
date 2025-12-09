package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_user_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationUserStatusBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private NotificationEventBO event;

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime readAt;


    @Builder.Default
    private Boolean isHidden = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

