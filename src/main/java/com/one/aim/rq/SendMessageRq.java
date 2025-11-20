package com.one.aim.rq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRq {
    private Long conversationId; // optional: if null, backend will create based on roomId
    private Long roomId;       // preferred: ADMIN_SELLER_{sellerId}
    private Long senderId;
    private String senderRole;   // ADMIN / SELLER / DELIVERY
    private String message;
    private String messageType;  // TEXT / IMAGE / FILE

}
