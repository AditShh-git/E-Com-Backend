package com.one.aim.bo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messagesBo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who sent
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private UserASDBO sender;

    // To which room
    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoomBo chatRoomBo;

    private String content;

    private LocalDateTime timestamp;

    private boolean readStatus = false; // unread by default
}
