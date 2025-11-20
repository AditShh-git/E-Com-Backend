package com.one.aim.bo;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_conversation",
       indexes = {
           @Index(name = "idx_conv_room", columnList = "room_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversationBo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Example values: ADMIN_SELLER_55, ADMIN_DELIVERY_22
    @Column(name = "room_id", nullable = false, unique = true, length = 120)
    private String roomId;

    // participant A — store id and role (lightweight approach)
    @Column(name = "participant_a_id")
    private Long participantAId;

    @Column(name = "participant_a_role", length = 30)
    private String participantARole;

    // participant B
    @Column(name = "participant_b_id")
    private Long participantBId;

    @Column(name = "participant_b_role", length = 30)
    private String participantBRole;

    @OneToMany(mappedBy = "conversationBo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessageBo> messages;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
