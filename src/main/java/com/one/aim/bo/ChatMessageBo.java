package com.one.aim.bo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_message",
       indexes = {
           @Index(name = "idx_msg_conv", columnList = "conversation_id"),
           @Index(name = "idx_msg_sender", columnList = "sender_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageBo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to ChatConversation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversationBo conversationBo;

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // id of admin/seller/delivery

    @Column(name = "sender_role", length = 30, nullable = false)
    private String senderRole; // ADMIN / SELLER / DELIVERY

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_type", length = 30)
    @Builder.Default
    private String messageType = "TEXT"; // TEXT / IMAGE / FILE

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "SENT"; // SENT / DELIVERED / READ

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
