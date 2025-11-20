package com.one.aim.rs;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRs {
    private Long id;
    private String roomId;
    private Long conversationId;
    private Long senderId;
    private String senderRole;
    private String message;
    private String messageType;
    private String status;
    private LocalDateTime createdAt;
}
